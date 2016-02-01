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
package org.jboss.wsf.stack.cxf;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.service.invoker.Invoker;
import org.jboss.wsf.stack.cxf.deployment.JNDIComponentResourceProvider;

/**
 * A JBossWS extension of the Apache CXF JAXRSInvoker invoker.
 * 
 * @author alessio.soldano@jboss.com
 * 
 */
public class JBossWSJAXRSInvoker extends JAXRSInvoker implements Invoker
{
   @Override
   protected Method getMethodToInvoke(ClassResourceInfo cri, OperationResourceInfo ori, Object resourceObject)
   {
      Method resourceMethod = cri.getMethodDispatcher().getMethod(ori);

      Method methodToInvoke = null;
      if (Proxy.class.isInstance(resourceObject))
      {
         methodToInvoke = cri.getMethodDispatcher().getProxyMethod(resourceMethod);
         if (methodToInvoke == null)
         {
            methodToInvoke = InjectionUtils.checkProxy(resourceMethod, resourceObject);
            cri.getMethodDispatcher().addProxyMethod(resourceMethod, methodToInvoke);
         }
      }
      else if (cri.getResourceProvider() instanceof JNDIComponentResourceProvider)
      {
         methodToInvoke = processJNDIRef(resourceMethod, resourceObject);
      }
      else
      {
         methodToInvoke = resourceMethod;
      }
      return methodToInvoke;
   }

   private Method processJNDIRef(Method methodToInvoke, Object resourceObject)
   {
      String methodToInvokeName = methodToInvoke.getName();
      Class<?>[] methodToInvokeTypes = methodToInvoke.getParameterTypes();

      for (Class<?> c : resourceObject.getClass().getInterfaces())
      {
         try
         {
            return c.getMethod(methodToInvokeName, methodToInvokeTypes);
         }
         catch (NoSuchMethodException ex)
         {
            //ignore
         }
         if (methodToInvokeTypes.length > 0)
         {
            for (Method m : c.getMethods())
            {
               if (m.getName().equals(methodToInvokeName) && m.getParameterTypes().length == methodToInvokeTypes.length)
               {
                  Class<?>[] methodTypes = m.getParameterTypes();
                  for (int i = 0; i < methodTypes.length; i++)
                  {
                     if (!methodTypes[i].isAssignableFrom(methodToInvokeTypes[i]))
                     {
                        break;
                     }
                  }
                  return m;
               }
            }
         }
      }
      return methodToInvoke;
   }
}
