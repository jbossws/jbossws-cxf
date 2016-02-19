/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * This Extension handles default scopes for discovered JAX-RS components. It
 * also observes ProcessInjectionTarget event and wraps InjectionTargets
 * representing JAX-RS components within JaxrsInjectionTarget. Furthermore, it
 * builds the sessionBeanInterface map which maps Session Bean classes to a
 * local interface. This map is used in CdiInjectorFactory during lookup of
 * Sesion Bean JAX-RS components.
 *
 * @author Jozef Hartinger
 *
 */
public class JaxRsCdiExtension implements Extension
{
   private static boolean active;

   private BeanManager beanManager;

   private static final String JAVAX_EJB_STATELESS = "javax.ejb.Stateless";

   private static final String JAVAX_EJB_SINGLETON = "javax.ejb.Singleton";

   private final List<Class<?>> providers = new ArrayList<>();

   private final List<Class<?>> resources = new ArrayList<>();

   // Scope literals
   public static final Annotation requestScopedLiteral = new AnnotationLiteral<RequestScoped>()
   {
      private static final long serialVersionUID = 3381824686081435817L;
   };

   public static final Annotation applicationScopedLiteral = new AnnotationLiteral<ApplicationScoped>()
   {
      private static final long serialVersionUID = -8211157243671012820L;
   };

   public static boolean isCDIActive()
   {
      return active;
   }

   private Map<Class<?>, Type> sessionBeanInterface = new HashMap<Class<?>, Type>();

   /**
    * Obtain BeanManager reference for future use.
    */
   public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager)
   {
      this.beanManager = beanManager;
      active = true;
   }

   /**
    * Set a default scope for each CDI bean which is a JAX-RS Resource.
    *
    * @param event
    * @param beanManager
    */
   public <T> void observeResources(@WithAnnotations(
   {Path.class}) @Observes ProcessAnnotatedType<T> event, BeanManager beanManager)
   {
      setBeanManager(beanManager);
      AnnotatedType<T> annotatedType = event.getAnnotatedType();

      if (!annotatedType.getJavaClass().isInterface() && !isSessionBean(annotatedType)
            // This check is redundant for CDI 1.1 containers but required for CDI 1.0
            && getClassAnnotation(annotatedType.getJavaClass(), Path.class) != null
            && !annotatedType.isAnnotationPresent(Decorator.class))
      {
         /*LogMessages.LOGGER.debug(Messages.MESSAGES.discoveredCDIBeanJaxRsResource(annotatedType.getJavaClass()
               .getCanonicalName()));*/
         event.setAnnotatedType(wrapAnnotatedType(annotatedType, requestScopedLiteral));
         this.resources.add(annotatedType.getJavaClass());
      }
   }
   
   private static <A extends Annotation> A getClassAnnotation(Class<?> c, Class<A> aClass) {
      if (c == null) {
          return null;
      }
      A p = c.getAnnotation(aClass);
      if (p != null) {
          return p;
      }

      p = getClassAnnotation(c.getSuperclass(), aClass);
      if (p != null) {
          return p;
      }

      // finally try the first one on the interface
      for (Class<?> i : c.getInterfaces()) {
          p = getClassAnnotation(i, aClass);
          if (p != null) {
              return p;
          }
      }
      return null;
  }

   /**
    * Set a default scope for each CDI bean which is a JAX-RS Provider.
    *
    * @param event
    * @param beanManager
    */
   public <T> void observeProviders(@WithAnnotations(
   {Provider.class}) @Observes ProcessAnnotatedType<T> event, BeanManager beanManager)
   {
      setBeanManager(beanManager);
      AnnotatedType<T> annotatedType = event.getAnnotatedType();

      if (!annotatedType.getJavaClass().isInterface() && !isSessionBean(annotatedType)
      // This check is redundant for CDI 1.1 containers but required for CDI 1.0
            && annotatedType.isAnnotationPresent(Provider.class))
      {
        /* LogMessages.LOGGER.debug(Messages.MESSAGES.discoveredCDIBeanJaxRsProvider(annotatedType.getJavaClass()
               .getCanonicalName()));*/
         event.setAnnotatedType(wrapAnnotatedType(annotatedType, applicationScopedLiteral));
         this.providers.add(annotatedType.getJavaClass());
      }
   }

   /**
    * Set a default scope for each CDI bean which is a JAX-RS Application subclass.
    *
    * @param event
    * @param beanManager
    */
   public <T extends Application> void observeApplications(@Observes ProcessAnnotatedType<T> event,
         BeanManager beanManager)
   {
      setBeanManager(beanManager);
      AnnotatedType<T> annotatedType = event.getAnnotatedType();

      if (!isSessionBean(annotatedType))
      {
         /*LogMessages.LOGGER.debug(Messages.MESSAGES.discoveredCDIBeanApplication(annotatedType.getJavaClass()
               .getCanonicalName()));*/
         event.setAnnotatedType(wrapAnnotatedType(annotatedType, applicationScopedLiteral));
      }
   }

   protected <T> AnnotatedType<T> wrapAnnotatedType(AnnotatedType<T> type, Annotation scope)
   {
      if (Utils.isScopeDefined(type, beanManager))
      {
        /* LogMessages.LOGGER.debug(Messages.MESSAGES.beanHasScopeDefined(type.getJavaClass()));*/
         return type; // leave it as it is
      }
      else
      {
         /*LogMessages.LOGGER.debug(Messages.MESSAGES.beanDoesNotHaveScopeDefined(type.getJavaClass(), scope));*/
         return new JaxrsAnnotatedType<T>(type, scope);
      }
   }

   /**
   /**
    * Observes ProcessSessionBean events and creates a (Bean class -> Local
    * interface) map for Session beans with local interfaces. This map is
    * necessary since RESTEasy identifies a bean class as JAX-RS components
    * while CDI requires a local interface to be used for lookup.
    */
   public <T> void observeSessionBeans(@Observes ProcessSessionBean<T> event)
   {
      Bean<Object> sessionBean = event.getBean();

      if (Utils.isJaxrsComponent(sessionBean.getBeanClass()))
      {
         addSessionBeanInterface(sessionBean);
      }
   }

   private void addSessionBeanInterface(Bean<?> bean)
   {
      for (Type type : bean.getTypes())
      {
         if ((type instanceof Class<?>) && ((Class<?>) type).isInterface())
         {
            Class<?> clazz = (Class<?>) type;
            if (Utils.isJaxrsAnnotatedClass(clazz))
            {
               sessionBeanInterface.put(bean.getBeanClass(), type);
               /*LogMessages.LOGGER.debug(Messages.MESSAGES.typeWillBeUsedForLookup(type, bean.getBeanClass()));*/
               return;
            }
         }
      }
      /*LogMessages.LOGGER.debug(Messages.MESSAGES.noLookupInterface(bean.getBeanClass()));*/
   }

   public Map<Class<?>, Type> getSessionBeanInterface()
   {
      return sessionBeanInterface;
   }

   private boolean isSessionBean(AnnotatedType<?> annotatedType)
   {
      for (Annotation annotation : annotatedType.getAnnotations())
      {
         Class<?> annotationType = annotation.annotationType();
         if (annotationType.getName().equals(JAVAX_EJB_STATELESS)
               || annotationType.getName().equals(JAVAX_EJB_SINGLETON))
         {
            return true; // Do not modify scopes of SLSBs and Singletons
         }
      }
      return false;
   }

   private void setBeanManager(BeanManager beanManager)
   {
      if (this.beanManager == null)
      {
         // this may happen if Solder Config receives BBD first
         this.beanManager = beanManager;
      }
   }

   public List<Class<?>> getProviders()
   {
      return providers;
   }

   public List<Class<?>> getResources()
   {
      return resources;
   }
}
