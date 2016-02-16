/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.deployment.aspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.xml.ws.spi.Provider;

import  com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationOutInterceptor;
import org.apache.cxf.jaxrs.validation.ValidationExceptionMapper;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.JAXRSDeploymentMetadata;
import org.jboss.wsf.stack.cxf.JBossWSJAXRSInvoker;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.cdi.CDIResourceProvider;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.deployment.JNDIComponentResourceProvider;


/**
 * A deployment aspect that creates the CXF Bus early and attaches it to the deployment
 *
 * @author alessio.soldano@jboss.com
 */
//TODO!!! unify with JAXWS handling
public class JAXRSBusDeploymentAspect extends AbstractDeploymentAspect
{

   @Override
   public void start(final Deployment dep)
   {
      if (BusFactory.getDefaultBus(false) == null)
      {
         //Make sure the default bus is created and set for client side usage
         //(i.e. no server side integration contribution in it)
         //TODO!!! think about this... is it still fine for the default bus to be created like this?
         JBossWSBusFactory.getDefaultBus(Provider.provider().getClass().getClassLoader());
      }     
      startDeploymentBus(dep);
   }

   @Override
   public void stop(final Deployment dep)
   {
      final Bus bus = dep.removeAttachment(Bus.class);
      if (bus != null)
      {
         bus.shutdown(true);
      }
   }

   private void startDeploymentBus(final Deployment dep)
   {
      BusFactory.setThreadDefaultBus(null);
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         ClassLoader classLoader = new DelegateClassLoader(dep.getClassLoader(), origClassLoader);
         SecurityActions.setContextClassLoader(classLoader);
         //at least for now, we use the classloader-bus association as a shortcut for bus retieval in the servlet...
         Bus bus = JBossWSBusFactory.getClassLoaderDefaultBus(dep.getClassLoader());
         //Force servlet transport to prevent CXF from using Jetty / http server or other transports
         bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
         //Don't add the default cxf JSONProvider
         bus.setProperty("skip.default.json.provider.registration", true);
         JAXRSDeploymentMetadata md = dep.getAttachment(JAXRSDeploymentMetadata.class);
         boolean cdiDeployment = false;
         if (dep.getProperty("isWeldDeployment") != null) {
            cdiDeployment = true;
         }
         List<Class<?>> applications = md.getScannedApplicationClasses();
         if (!applications.isEmpty()) {
            for (Class<?> appClazz : applications) {
               createFromApplication(md, appClazz, bus, classLoader, cdiDeployment);
            }
         } else {
            create(md, bus, classLoader, cdiDeployment);
         }
         dep.addAttachment(Bus.class, bus);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }
   
   private static void createFromApplication(JAXRSDeploymentMetadata md, Class<?> appClazz, Bus bus, ClassLoader classLoader, boolean cdiDeployment) {
      ApplicationInfo providerApp = (ApplicationInfo)createSingletonInstance(appClazz, bus);
      Application app = providerApp.getProvider();
      JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, md.isIgnoreApplicationPath(), false);
      bean.setBus(bus);
      bean.setApplicationInfo(providerApp);
      bean.setInvoker(new JBossWSJAXRSInvoker());
      List<Class<?>> additionalResources = new ArrayList<>();
      if (app.getClasses().isEmpty() && app.getSingletons().isEmpty()) {
         processResources(bean, md, bus, classLoader, additionalResources, cdiDeployment);
         setProviders(bean, md, bus, classLoader);
      }
      if (!cdiDeployment) {
         processJNDIComponentResources(bean, md, bus, classLoader, additionalResources);
      }
     
      setJSONProviders(bean);
      setValidationInterceptors(bean);
      if (!bean.getResourceClasses().isEmpty() || !additionalResources.isEmpty()) {
         bean.setResourceClasses(additionalResources);
         bean.create();
      }
   }
   
   private static void setValidationInterceptors(JAXRSServerFactoryBean bean) {
      bean.setInInterceptors(Arrays.<Interceptor<? extends Message>> asList(new JAXRSBeanValidationInInterceptor()));
      bean.setOutInterceptors(Arrays.<Interceptor<? extends Message>> asList(new JAXRSBeanValidationOutInterceptor()));
      bean.setProvider(new ValidationExceptionMapper());
   }
   
   private static void create(JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader, boolean cdiDeployment) {
      JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
      bean.setBus(bus);
      bean.setAddress("/"); //TODO!!!
      bean.setInvoker(new JBossWSJAXRSInvoker());
      //resources...
      List<Class<?>> resources = new ArrayList<>();
      processResources(bean, md, bus, classLoader, resources, cdiDeployment);
      //resource providers (CXF)... ?
      
      //providers...
      setProviders(bean, md, bus, classLoader);
      //jndi resource...
      processJNDIComponentResources(bean, md, bus, classLoader, resources);
      setJSONProviders(bean);
      setValidationInterceptors(bean);
      if (!bean.getResourceClasses().isEmpty() || !resources.isEmpty()) {
         bean.setResourceClasses(resources);
         bean.create();
      }
   }
   
   private static void processResources(JAXRSServerFactoryBean bean, JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader, List<Class<?>> resources, boolean cdiDeployment) {
      if (!md.getScannedResourceClasses().isEmpty()) {
         try {
            for (String cl : md.getScannedResourceClasses()) 
            {
               Class<?> clazz = classLoader.loadClass(cl);
               resources.add(clazz);
               if (cdiDeployment) {
                  bean.setResourceProvider(clazz, new CDIResourceProvider<>(clazz));
               }
            }
         } catch (ClassNotFoundException cnfe) {
            throw new WSFException(cnfe);
         }
      }
   }
   
   private static void setProviders(JAXRSServerFactoryBean bean, JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader) {
      if (!md.getScannedProviderClasses().isEmpty()) {
         List<Object> providers = new ArrayList<>();
         try {
            for (String cl : md.getScannedProviderClasses()) {
               Class<?> clazz = classLoader.loadClass(cl);
               providers.add(createSingletonInstance(clazz, bus));
            }
         } catch (ClassNotFoundException cnfe) {
            throw new WSFException(cnfe);
         }
         bean.setProviders(providers);
      }
   }
   
   private static void processJNDIComponentResources(JAXRSServerFactoryBean bean, JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader, List<Class<?>> resources) {
      if (!md.getScannedJndiComponentResources().isEmpty()) {
         try {
            for (String cl : md.getScannedJndiComponentResources()) {
               String[] config = cl.trim().split(";");
               if (config.length < 3) {
                  throw Messages.MESSAGES.jndiComponentResourceNotSetCorrectly();
               }
               String jndiName = config[0];
               Class<?> clazz = classLoader.loadClass(config[1]);
               boolean cacheRefrence = Boolean.valueOf(config[2].trim());
               resources.add(clazz);
               bean.setResourceProvider(clazz, new JNDIComponentResourceProvider(jndiName, clazz, cacheRefrence));
            }
         } catch (ClassNotFoundException cnfe) {
            throw new WSFException(cnfe);
         }
      }
   }
   
   private static void setJSONProviders(JAXRSServerFactoryBean bean) {
      //Add default Jettison provider
      @SuppressWarnings("rawtypes")
      JSONProvider jsonProvider = new JSONProvider();
      jsonProvider.setDropRootElement(true);
      bean.setProvider(jsonProvider);
      JacksonJsonProvider provider = new JacksonJsonProvider();
      bean.setProvider(provider);
   }

   private static Object createSingletonInstance(Class<?> cls, Bus bus)
   {
      Constructor<?> c = ResourceUtils.findResourceConstructor(cls, false);
      if (c == null)
      {
         throw new WSFException("No valid constructor found for " + cls.getName());
      }
      boolean isApplication = Application.class.isAssignableFrom(c.getDeclaringClass());
      try
      {
         ProviderInfo<? extends Object> provider = null;
         if (c.getParameterTypes().length == 0)
         {
            if (isApplication)
            {
               provider = new ApplicationInfo((Application) c.newInstance(), bus);
            }
            else
            {
               provider = new ProviderInfo<Object>(c.newInstance(), bus, false, true);
            }
         }
         else
         {
            provider = ProviderFactory.createProviderFromConstructor(c, null, bus, isApplication, true);
         }
         Object instance = provider.getProvider();
         return isApplication ? provider : instance;
      }
      catch (InstantiationException ex)
      {
         ex.printStackTrace();
         throw new WSFException("Resource class " + cls.getName() + " can not be instantiated");
      }
      catch (IllegalAccessException ex)
      {
         ex.printStackTrace();
         throw new WSFException("Resource class " + cls.getName() + " can not be instantiated due to IllegalAccessException");
      }
      catch (InvocationTargetException ex)
      {
         ex.printStackTrace();
         throw new WSFException("Resource class " + cls.getName() + " can not be instantiated due to InvocationTargetException");
      }
   }
}
