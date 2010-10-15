/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;

/**
 * This ServiceObjectFactory reconstructs a javax.xml.ws.Service
 * for a given WSDL when the webservice client does a JNDI lookup.
 *
 * @see CXFServiceReferenceableJAXWS
 *
 * @author Thomas.Diesler@jboss.com
 * @author Richard.Opalka@jboss.com
 * @author alessio.soldano@jboss.com
 */
public class CXFServiceObjectFactoryJAXWS implements ObjectFactory
{
   protected final Logger log = Logger.getLogger(CXFServiceObjectFactoryJAXWS.class);

   /**
    * Creates an object using the location or reference information specified.
    * <p/>
    *
    * @param obj         The possibly null object containing location or reference
    *                    information that can be used in creating an object.
    * @param name        The name of this object relative to <code>nameCtx</code>,
    *                    or null if no name is specified.
    * @param nameCtx     The context relative to which the <code>name</code>
    *                    parameter is specified, or null if <code>name</code> is
    *                    relative to the default initial context.
    * @param environment The possibly null environment that is used in
    *                    creating the object.
    * @return The object created; null if an object cannot be created.
    * @throws Exception if this object factory encountered an exception
    *                   while attempting to create an object, and no other object factories are
    *                   to be tried.
    * @see javax.naming.spi.NamingManager#getObjectInstance
    * @see javax.naming.spi.NamingManager#getURLContext
    */
   @SuppressWarnings(value = "unchecked")
   public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable environment)
         throws Exception
   {
      try
      {
         // references
         final Reference ref = (Reference) obj;
         final UnifiedServiceRefMetaData serviceRef = unmarshallServiceRef(ref);
         // class names
         final String serviceImplClass = this.getServiceClassName(ref, serviceRef);
         final String targetClassName = this.getTargetClassName(ref, serviceRef, serviceImplClass);
         // class instances
         final Class<?> serviceClass = this.getClass(serviceImplClass);
         final Class<?> targetClass = this.getClass(targetClassName);
         // clean thread local bus before constructing Service
         BusFactory.setThreadDefaultBus(null);
         try
         {
            // construct service
            final Bus bus = this.createNewBus(serviceRef);
            final Service serviceInstance = this.instantiateService(serviceRef, serviceClass);
            if (serviceRef.getHandlerChain() != null)
            {
               serviceInstance.setHandlerResolver(new CXFHandlerResolverImpl(bus, serviceRef.getHandlerChain(),
                     serviceInstance.getClass()));
            }
            // construct port
            final boolean instantiatePort = targetClassName != null && !targetClassName.equals(serviceImplClass);
            if (instantiatePort)
            {
               final QName portQName = this.getPortQName(targetClassName, serviceImplClass, serviceRef);
               final WebServiceFeature[] portFeatures = this.getFeatures(targetClassName, serviceImplClass, serviceRef);

               return instantiatePort(serviceClass, targetClass, serviceInstance, portQName, portFeatures);
            }

            return serviceInstance;
         }
         finally 
         {
            BusFactory.setThreadDefaultBus(null);
         }
      }
      catch (Exception ex)
      {
         WSFException.rethrow("Cannot create service", ex);
      }

      return null;
   }

   private Class<?> getClass(final String className) throws ClassNotFoundException
   {
      if (className != null)
      {
         return Thread.currentThread().getContextClassLoader().loadClass(className);
      }

      return null;
   }

   private Bus createNewBus(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final Bus bus;
      final URL cxfConfig = this.getCXFConfiguration(serviceRefMD.getVfsRoot());
      if (cxfConfig != null)
      {
         final SpringBusFactory busFactory = new JBossWSSpringBusFactory();
         bus = busFactory.createBus(cxfConfig);
         BusFactory.setThreadDefaultBus(bus);
      }
      else
      {
         Bus threadBus = BusFactory.getThreadDefaultBus(false);
         bus = threadBus != null ? threadBus : BusFactory.newInstance().createBus();
      }

      //Add extension to configure stub properties using the UnifiedServiceRefMetaData 
      Configurer configurer = bus.getExtension(Configurer.class);
      bus.setExtension(new CXFServiceRefStubPropertyConfigurer(serviceRefMD, configurer), Configurer.class);

      return bus;
   }

   private String getServiceClassName(final Reference ref, final UnifiedServiceRefMetaData serviceRefMD)
   {
      String serviceClassName = serviceRefMD.getServiceImplClass();
      if (serviceClassName == null)
         serviceClassName = (String) ref.get(CXFServiceReferenceableJAXWS.SERVICE_IMPL_CLASS).getContent();

      return serviceClassName;
   }

   private String getTargetClassName(final Reference ref, final UnifiedServiceRefMetaData serviceRefMD,
         final String serviceImplClass)
   {
      String targetClassName = serviceRefMD.getServiceRefType();
      if (targetClassName == null)
         targetClassName = (String) ref.get(CXFServiceReferenceableJAXWS.TARGET_CLASS_NAME).getContent();

      if (Service.class.getName().equals(targetClassName))
         targetClassName = serviceImplClass;

      return targetClassName;
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
               port = method.invoke(target, new Object[0]);
               retVal = port;
               break;
            }
         }
      }

      if (port == null)
      {
         Method method = getMethodFor(portQName, features, serviceClass);
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
      final URL wsdlURL = serviceRefMD.getWsdlLocation();
      final QName serviceQName = serviceRefMD.getServiceQName();

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
            throw new IllegalArgumentException("Cannot create generic javax.xml.ws.Service without wsdlLocation: "
                  + serviceRefMD);
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

   private Method getMethodFor(final QName portQName, final WebServiceFeature[] features, final Class<?> serviceClass)
         throws NoSuchMethodException
   {
      if ((portQName == null) && (features == null))
         return serviceClass.getMethod("getPort", new Class[]
         {Class.class});
      if ((portQName != null) && (features == null))
         return serviceClass.getMethod("getPort", new Class[]
         {QName.class, Class.class});
      if ((portQName == null) && (features != null))
         return serviceClass.getMethod("getPort", new Class[]
         {Class.class, WebServiceFeature[].class});
      if ((portQName != null) && (features != null))
         return serviceClass.getMethod("getPort", new Class[]
         {QName.class, Class.class, WebServiceFeature[].class});

      throw new IllegalStateException();
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

   private WebServiceFeature[] getFeatures(final UnifiedServiceRefMetaData serviceRef)
   {
      List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();

      // configure @Addressing feature
      if (serviceRef.isAddressingEnabled())
      {
         final boolean required = serviceRef.isAddressingRequired();
         final String refResponses = serviceRef.getAddressingResponses();
         AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
         if ("ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.ANONYMOUS;
         if ("NON_ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.NON_ANONYMOUS;

         features.add(new AddressingFeature(true, required, responses));
      }

      // configure @MTOM feature
      if (serviceRef.isMtomEnabled())
      {
         features.add(new MTOMFeature(true, serviceRef.getMtomThreshold()));
      }

      // configure @RespectBinding feature
      if (serviceRef.isRespectBindingEnabled())
      {
         features.add(new RespectBindingFeature(true));
      }

      WebServiceFeature[] wsFeatures = features.size() == 0 ? null : features.toArray(new WebServiceFeature[]
      {});
      return wsFeatures;
   }

   private WebServiceFeature[] getFeatures(final UnifiedPortComponentRefMetaData portComponentRefMD)
   {
      List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
      // configure @Addressing feature
      if (portComponentRefMD.isAddressingEnabled())
      {
         final boolean required = portComponentRefMD.isAddressingRequired();
         final String refResponses = portComponentRefMD.getAddressingResponses();
         AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
         if ("ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.ANONYMOUS;
         if ("NON_ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.NON_ANONYMOUS;

         features.add(new AddressingFeature(true, required, responses));
      }

      // configure @MTOM feature
      if (portComponentRefMD.isMtomEnabled())
      {
         features.add(new MTOMFeature(true, portComponentRefMD.getMtomThreshold()));
      }

      // configure @RespectBinding feature
      if (portComponentRefMD.isRespectBindingEnabled())
      {
         features.add(new RespectBindingFeature(true));
      }

      return features.size() == 0 ? null : features.toArray(new WebServiceFeature[]
      {});
   }

   private UnifiedServiceRefMetaData unmarshallServiceRef(final Reference ref) throws ClassNotFoundException,
         NamingException
   {
      final UnifiedServiceRefMetaData sref;
      final RefAddr refAddr = ref.get(CXFServiceReferenceableJAXWS.SERVICE_REF_META_DATA);
      final ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) refAddr.getContent());
      try
      {
         ObjectInputStream ois = new ObjectInputStream(bais);
         sref = (UnifiedServiceRefMetaData) ois.readObject();
         ois.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot unmarshall service ref meta data, cause: " + e.toString());
      }

      return sref;
   }

   private URL getCXFConfiguration(final UnifiedVirtualFile vfsRoot)
   {
      URL url = null;
      try
      {
         url = vfsRoot.findChild("WEB-INF/jbossws-cxf.xml").toURL();
      }
      catch (Exception e)
      {
      }

      if (url == null)
      {
         try
         {
            url = vfsRoot.findChild("META-INF/jbossws-cxf.xml").toURL();
         }
         catch (Exception e)
         {
         }
      }
      return url;
   }
}
