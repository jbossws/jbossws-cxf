/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.stack.cxf.client.serviceref;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.jboss.ws.common.Messages;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * This ServiceObjectFactory reconstructs a javax.xml.ws.Service
 * for a given WSDL when the webservice client does a JNDI lookup.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceObjectFactoryJAXWS
{
   public final Object getObjectInstance(UnifiedServiceRefMetaData serviceRef)
   {
      try
      {
         // class names
         final String serviceImplClass = this.getServiceClassName(serviceRef);
         final String targetClassName = this.getTargetClassName(serviceRef);
         // class instances
         final Class<?> serviceClass = this.getClass(serviceImplClass);
         final Class<?> targetClass = this.getClass(targetClassName);
         final Service serviceInstance;

         this.init(serviceRef);
         try
         {
            serviceInstance = this.instantiateService(serviceRef, serviceClass);
            this.configure(serviceRef, serviceInstance);

            // construct port
            final boolean instantiatePort = targetClassName != null && !Service.class.isAssignableFrom(targetClass);
            if (instantiatePort)
            {
               final QName portQName = this.getPortQName(targetClassName, serviceImplClass, serviceRef);
               final WebServiceFeature[] portFeatures = this.getFeatures(targetClassName, serviceImplClass, serviceRef);

               return instantiatePort(serviceClass, targetClass, serviceInstance, portQName, portFeatures);
            }
         }
         finally
         {
            this.destroy(serviceRef);
         }

         return serviceInstance;
      }
      catch (Exception ex)
      {
         WSFException.rethrow("Cannot create service", ex);
      }

      return null;
   }

   /**
    * Lifecycle template method called before javax.xml.ws.Service object instantiation.
    *
    * @param serviceRefUMDM service reference meta data
    */
   protected abstract void init(final UnifiedServiceRefMetaData serviceRefUMDM);

   /**
    * Lifecycle template method called after javax.xml.ws.Service object was created
    * and before port is instantiated. It allows stack to configure service before
    * creating ports.
    *
    * @param serviceRefUMDM service reference meta data
    * @param service service instance
    */
   protected abstract void configure(final UnifiedServiceRefMetaData serviceRefUMDM, final Service service);

   /**
    * Lifecycle template method called after javax.xml.ws.Service object and after port instantiation.
    *
    * @param serviceRefUMDM
    */
   protected abstract void destroy(final UnifiedServiceRefMetaData serviceRefUMDM);

   private Class<?> getClass(final String className) throws ClassNotFoundException
   {
      if (className != null)
      {
         return Thread.currentThread().getContextClassLoader().loadClass(className);
      }

      return null;
   }

   private String getServiceClassName(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final String serviceImplClassName = serviceRefMD.getServiceImplClass();
      if (serviceImplClassName != null)
         return serviceImplClassName;

      final String serviceInterfaceName = serviceRefMD.getServiceInterface();
      if (serviceInterfaceName != null)
         return serviceInterfaceName;

      return Service.class.getName(); // fallback
   }

   private String getTargetClassName(final UnifiedServiceRefMetaData serviceRefMD)
   {
      return serviceRefMD.getServiceRefType();
   }

   private Object instantiatePort(final Class<?> serviceClass, final Class<?> targetClass, final Service target,
         final QName portQName, final WebServiceFeature[] features) throws NoSuchMethodException,
         InstantiationException, IllegalAccessException, InvocationTargetException
   {
      Object retVal = null;

      Object port = null;
      if (serviceClass != Service.class)
      {
         for (Method method : serviceClass.getDeclaredMethods())
         {
            String methodName = method.getName();
            Class<?> retType = method.getReturnType();
            if (methodName.startsWith("get") && targetClass.isAssignableFrom(retType))
            {
               final Method targetMethod = getMethodFor(methodName, features, serviceClass);
               final Object[] args = getArgumentsFor(features);
               port = targetMethod.invoke(target, args);
               retVal = port;
               break;
            }
         }
      }

      if (port == null)
      {
         Method method = getMethodFor("getPort", portQName, features, serviceClass);
         Object[] args = getArgumentsFor(portQName, features, targetClass);
         port = method.invoke(target, args);
         retVal = port;
      }

      return retVal;
   }

   private Service instantiateService(final UnifiedServiceRefMetaData serviceRefMD, final Class<?> serviceClass)
         throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      final WebServiceFeature[] features = getFeatures(serviceRefMD);
      final URL wsdlURL = this.getWsdlURL(serviceRefMD, serviceClass);
      final QName serviceQName = this.getServiceQName(serviceRefMD, serviceClass);

      Service target = null;
      if (serviceClass == Service.class)
      {
         // Generic javax.xml.ws.Service
         if (wsdlURL != null)
         {
            if (features != null)
            {
               target = Service.create(wsdlURL, serviceQName, features);
            }
            else
            {
               target = Service.create(wsdlURL, serviceQName);
            }
         }
         else
         {
            throw Messages.MESSAGES.cannotCreateServiceWithoutWsdlLocation(serviceRefMD);
         }
      }
      else
      {
         // Generated javax.xml.ws.Service subclass
         if (wsdlURL != null)
         {
            if (features != null)
            {
               Constructor<?> ctor = serviceClass.getConstructor(new Class[]
               {URL.class, QName.class, WebServiceFeature[].class});
               target = (Service) ctor.newInstance(new Object[]
               {wsdlURL, serviceQName, features});
            }
            else
            {
               Constructor<?> ctor = serviceClass.getConstructor(new Class[]
               {URL.class, QName.class});
               target = (Service) ctor.newInstance(new Object[]
               {wsdlURL, serviceQName});
            }
         }
         else
         {
            if (features != null)
            {
               Constructor<?> ctor = serviceClass.getConstructor(new Class[]
               {WebServiceFeature[].class});
               target = (Service) ctor.newInstance(new Object[]
               {features});
            }
            else
            {
               target = (Service) serviceClass.newInstance();
            }
         }
      }

      return target;
   }

   private URL getWsdlURL(final UnifiedServiceRefMetaData serviceRefMD, final Class<?> serviceClass)
   {
      if (serviceRefMD.getWsdlLocation() == null)
      {
         final WebServiceClient webServiceClientAnnotation = serviceClass.getAnnotation(WebServiceClient.class);
         if (webServiceClientAnnotation != null)
         {
            // use the @WebServiceClien(wsdlLocation=...) if the service ref wsdl location returned at this time would be null
            if (webServiceClientAnnotation.wsdlLocation().length() > 0)
            {
               serviceRefMD.setWsdlOverride(webServiceClientAnnotation.wsdlLocation());
            }
         }
      }

      return serviceRefMD.getWsdlLocation();
   }

   private QName getServiceQName(final UnifiedServiceRefMetaData serviceRefMD, final Class<?> serviceClass)
   {
      QName retVal = serviceRefMD.getServiceQName();

      if (retVal == null)
      {
         final WebServiceClient webServiceClientAnnotation = serviceClass.getAnnotation(WebServiceClient.class);
         if (webServiceClientAnnotation != null)
         {
            retVal = new QName(webServiceClientAnnotation.targetNamespace(), webServiceClientAnnotation.name());
         }
      }

      return retVal;
   }

   private WebServiceFeature[] getFeatures(final String targetClassName, final String serviceClassName,
         final UnifiedServiceRefMetaData serviceRefMD)
   {
      if (targetClassName != null && !targetClassName.equals(serviceClassName))
      {
         final Collection<UnifiedPortComponentRefMetaData> portComponentRefs = serviceRefMD.getPortComponentRefs();
         for (final UnifiedPortComponentRefMetaData portComponentRefMD : portComponentRefs)
         {
            if (targetClassName.equals(portComponentRefMD.getServiceEndpointInterface()))
            {
               return getFeatures(portComponentRefMD);
            }
         }
      }

      return null;
   }

   private QName getPortQName(final String targetClassName, final String serviceClassName,
         final UnifiedServiceRefMetaData serviceRefMD)
   {
      if (targetClassName != null && !targetClassName.equals(serviceClassName))
      {
         final Collection<UnifiedPortComponentRefMetaData> portComponentRefs = serviceRefMD.getPortComponentRefs();
         for (final UnifiedPortComponentRefMetaData portComponentRefMD : portComponentRefs)
         {
            if (targetClassName.equals(portComponentRefMD.getServiceEndpointInterface()))
            {
               return portComponentRefMD.getPortQName();
            }
         }
      }

      return null;
   }

   private Method getMethodFor(final String methodName, final QName portQName, final WebServiceFeature[] features, final Class<?> serviceClass)
         throws NoSuchMethodException
   {
      if ((portQName == null) && (features == null))
         return serviceClass.getMethod(methodName, new Class[]
         {Class.class});
      if ((portQName != null) && (features == null))
         return serviceClass.getMethod(methodName, new Class[]
         {QName.class, Class.class});
      if ((portQName == null) && (features != null))
         return serviceClass.getMethod(methodName, new Class[]
         {Class.class, WebServiceFeature[].class});
      if ((portQName != null) && (features != null))
         return serviceClass.getMethod(methodName, new Class[]
         {QName.class, Class.class, WebServiceFeature[].class});

      throw new IllegalStateException();
   }

   private Method getMethodFor(final String methodName, final WebServiceFeature[] features, final Class<?> serviceClass)
   throws NoSuchMethodException
   {
      if (features == null)
      {
         return serviceClass.getMethod(methodName, new Class[] {});
      }
      else
      {
         return serviceClass.getMethod(methodName, new Class[] { WebServiceFeature[].class } );
      }
   }

   private Object[] getArgumentsFor(final QName portQName, final WebServiceFeature[] features,
         final Class<?> targetClass) throws NoSuchMethodException
   {
      if ((portQName == null) && (features == null))
         return new Object[]
         {targetClass};
      if ((portQName != null) && (features == null))
         return new Object[]
         {portQName, targetClass};
      if ((portQName == null) && (features != null))
         return new Object[]
         {targetClass, features};
      if ((portQName != null) && (features != null))
         return new Object[]
         {portQName, targetClass, features};

      throw new IllegalStateException();
   }

   private Object[] getArgumentsFor(final WebServiceFeature[] features) throws NoSuchMethodException
   {
      if (features == null)
      {
         return new Object[] {};
      }
      else
      {
         return new Object[] {features};
      }
   }

   private WebServiceFeature[] getFeatures(final UnifiedServiceRefMetaData serviceRef)
   {
      List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();

      // configure @Addressing feature
      if (serviceRef.isAddressingAnnotationSpecified())
      {
         final boolean enabled = serviceRef.isAddressingEnabled();
         final boolean required = serviceRef.isAddressingRequired();
         final String refResponses = serviceRef.getAddressingResponses();
         AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
         if ("ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.ANONYMOUS;
         if ("NON_ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.NON_ANONYMOUS;

         features.add(new AddressingFeature(enabled, required, responses));
      }

      // configure @MTOM feature
      if (serviceRef.isMtomAnnotationSpecified())
      {
         final boolean enabled = serviceRef.isMtomEnabled();
         final int threshold = serviceRef.getMtomThreshold();
         features.add(new MTOMFeature(enabled, threshold));
      }

      // configure @RespectBinding feature
      if (serviceRef.isRespectBindingAnnotationSpecified())
      {
         final boolean enabled = serviceRef.isRespectBindingEnabled();
         features.add(new RespectBindingFeature(enabled));
      }

      return features.size() == 0 ? null : features.toArray(new WebServiceFeature[]
      {});
   }

   private WebServiceFeature[] getFeatures(final UnifiedPortComponentRefMetaData portComponentRefMD)
   {
      List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
      // configure @Addressing feature
      if (portComponentRefMD.isAddressingAnnotationSpecified())
      {
         final boolean enabled = portComponentRefMD.isAddressingEnabled();
         final boolean required = portComponentRefMD.isAddressingRequired();
         final String refResponses = portComponentRefMD.getAddressingResponses();
         AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
         if ("ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.ANONYMOUS;
         if ("NON_ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.NON_ANONYMOUS;

         features.add(new AddressingFeature(enabled, required, responses));
      }

      // configure @MTOM feature
      if (portComponentRefMD.isMtomEnabled())
      {
         features.add(new MTOMFeature(true, portComponentRefMD.getMtomThreshold()));
      }

      // configure @RespectBinding feature
      if (portComponentRefMD.isRespectBindingAnnotationSpecified())
      {
         final boolean enabled = portComponentRefMD.isRespectBindingEnabled();
         features.add(new RespectBindingFeature(enabled));
      }

      return features.size() == 0 ? null : features.toArray(new WebServiceFeature[]
      {});
   }
}
