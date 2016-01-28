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
import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.xml.ws.spi.Provider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.JAXRSDeploymentMetadata;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;


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
         
         List<Class<?>> applications = md.getScannedApplicationClasses();
         if (!applications.isEmpty()) {
            for (Class<?> appClazz : applications) {
               createFromApplication(md, appClazz, bus, classLoader);
            }
         } else {
            create(md, bus, classLoader);
         }
         dep.addAttachment(Bus.class, bus);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }
   
   private static void createFromApplication(JAXRSDeploymentMetadata md, Class<?> appClazz, Bus bus, ClassLoader classLoader) {
      ApplicationInfo providerApp = (ApplicationInfo)createSingletonInstance(appClazz, bus);
      Application app = providerApp.getProvider();
      JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, false, false);
      bean.setBus(bus);
      bean.setApplicationInfo(providerApp);
      if (!appClazz.isAnnotationPresent(ApplicationPath.class)) {
         if (app.getClasses().isEmpty() && app.getSingletons().isEmpty()) {
            setResources(bean, md, bus, classLoader);
            setProviders(bean, md, bus, classLoader);
         }
      }
      setJSONProviders(bean);
      bean.create();
   }
   
   private static void create(JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader) {
      JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();
      bean.setBus(bus);
      bean.setAddress("/"); //TODO!!!
      //resources...
      setResources(bean, md, bus, classLoader);
      //resource providers (CXF)... ?
      
      //jndi resource... ?
      
      //providers...
      setProviders(bean, md, bus, classLoader);
      setJSONProviders(bean);
      bean.create();
   }
   
   private static void setResources(JAXRSServerFactoryBean bean, JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader) {
      if (!md.getScannedResourceClasses().isEmpty()) {
         List<Class<?>> resources = new ArrayList<>();
         try {
            for (String cl : md.getScannedResourceClasses()) {
               resources.add(classLoader.loadClass(cl));
            }
         } catch (ClassNotFoundException cnfe) {
            throw new WSFException(cnfe);
         }
         bean.setResourceClasses(resources);
      }
   }
   
   private static void setProviders(JAXRSServerFactoryBean bean, JAXRSDeploymentMetadata md, Bus bus, ClassLoader classLoader) {
      if (!md.getScannedProviderClasses().isEmpty()) {
         List<Object> providers = new ArrayList<>();
         try {
            for (String cl : md.getScannedProviderClasses()) {
               Class<?> clazz = classLoader.loadClass(cl);
               providers.add((ApplicationInfo)createSingletonInstance(clazz, bus));
            }
         } catch (ClassNotFoundException cnfe) {
            throw new WSFException(cnfe);
         }
         bean.setProviders(providers);
      }
   }
   
   private static void setJSONProviders(JAXRSServerFactoryBean bean) {
      //Add default Jettison provider
      @SuppressWarnings("rawtypes")
      JSONProvider jsonProvider = new JSONProvider();
      jsonProvider.setDropRootElement(true);
      bean.setProvider(jsonProvider);
      //TODO: Add jackson provider
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
