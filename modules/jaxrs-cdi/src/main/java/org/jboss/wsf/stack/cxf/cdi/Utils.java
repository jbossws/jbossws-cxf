/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * Utility methods for detecting CDI scopes and JAX-RS components
 * @author Jozef Hartinger
 *
 */
public class Utils
{
   /**
    * Finds out if a given class is decorated with JAX-RS annotations.
    * Interfaces of the class are not scanned for JAX-RS annotations.
    *
    * @param clazz
    * @return true if a given interface has @Path annotation or if any of its
    *         methods is decorated with @Path annotation or a request method
    *         designator.
    */
   public static boolean isJaxrsAnnotatedClass(Class<?> clazz)
   {
      if (clazz.isAnnotationPresent(Path.class))
      {
         return true;
      }
      if (clazz.isAnnotationPresent(Provider.class))
      {
         return true;
      }
      for (Method method : clazz.getMethods())
      {
         if (method.isAnnotationPresent(Path.class))
         {
            return true;
         }
         for (Annotation annotation : method.getAnnotations())
         {
            if (annotation.annotationType().isAnnotationPresent(HttpMethod.class))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Returns true if and only if the given class is a JAX-RS root resource or a
    * sub-resource. The class itself as well as its interfaces are scanned for
    * JAX-RS annotations.
    */
   public static boolean isJaxrsResource(Class<?> clazz)
   {
      if (isJaxrsAnnotatedClass(clazz))
      {
         return true;
      }
      for (Class<?> intf : clazz.getInterfaces())
      {
         if (isJaxrsAnnotatedClass(intf))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Find out if a given class is a JAX-RS component
    *
    * @return true if and only if a give class is a JAX-RS resource, provider or
    *         javax.ws.rs.core.Application subclass.
    */
   public static boolean isJaxrsComponent(Class<?> clazz)
   {
      return ((clazz.isAnnotationPresent(Provider.class)) || (isJaxrsResource(clazz)) || (Application.class
            .isAssignableFrom(clazz)));
   }

   /**
    * Find out if a given annotated type is explicitly bound to a scope.
    *
    * @return true if and only if a given annotated type is annotated with a scope
    *         annotation or with a stereotype which (transitively) declares a
    *         scope
    */
   public static boolean isScopeDefined(AnnotatedType<?> annotatedType, BeanManager manager)
   {
      for (Annotation annotation : annotatedType.getAnnotations())
      {
         if (manager.isScope(annotation.annotationType()))
         {
            return true;
         }
         if (manager.isStereotype(annotation.annotationType()))
         {
            if (isScopeDefined(annotation.annotationType(), manager))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Find out if a given class is explicitly bound to a scope.
    *
    * @param clazz
    * @param manager
    * @return <code>true</code> if a given class is annotated with a scope
    *         annotation or with a stereotype which (transitively) declares a
    *         scope
    */
   private static boolean isScopeDefined(Class<?> clazz, BeanManager manager)
   {
      for (Annotation annotation : clazz.getAnnotations())
      {
         if (manager.isScope(annotation.annotationType()))
         {
            return true;
         }
         if (manager.isStereotype(annotation.annotationType()))
         {
            if (isScopeDefined(annotation.annotationType(), manager))
            {
               return true;
            }
         }
      }
      return false;
   }
}
