/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.jbws3223;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.spi.Provider;

public class Client
{
   private boolean checkClassLoader;
   
   public Client(boolean checkClassLoader)
   {
      this.checkClassLoader = checkClassLoader;
   }
   
   public String run(String param, URL wsdlURL)
   {
      ClassLoader orig = getContextClassLoader();
      try
      {
         // Create the port
         QName qname = new QName("http://org.jboss.ws/jaxws/jbws3223", "EndpointService");
         Service service = Service.create(wsdlURL, qname);
         checkContextClassLoaderInvariance(orig);
         EndpointInterface port = (EndpointInterface) service.getPort(EndpointInterface.class);
         checkContextClassLoaderInvariance(orig);
         String result = port.echo(param);
         checkContextClassLoaderInvariance(orig);
         if (checkClassLoader)
         {
            checkImplementationClassesAreNotVisible(orig);
         }
         return result;
      }
      finally
      {
         //set back the original TCCL: this is just a sanity fix to ensure a failure in this
         //test does not influence other tests; the TCCL is not expected to be changed
         setContextClassLoader(orig);
      }
   }

   private static void checkContextClassLoaderInvariance(ClassLoader reference)
   {
      ClassLoader tccl = getContextClassLoader();
      if (reference == null && tccl == null)
      {
         return;
      }
      if (reference == null || tccl == null || !tccl.equals(reference))
      {
         throw new RuntimeException("Thread context classloader changed from " + reference + " to " + tccl);
      }
   }
   
   /**
    * AS7 check on client TCCL
    * 
    * @param cl
    */
   private void checkImplementationClassesAreNotVisible(ClassLoader cl)
   {
      //retrieve the stack specific Provider impl class name through the JAXWS API
      //classloading mechanism (JBEE-75), which is able to "see" implementation classes
      String providerImplClassName = Provider.provider().getClass().getName();
      if (!providerImplClassName.contains("jboss"))
      {
         throw new RuntimeException("Expected a JBoss(WS) specific implementation for jakarta.xml.ws.spi.Provider: "
               + providerImplClassName);
      }
      //then try loading the same class using the provided classloader
      try
      {
         cl.loadClass(providerImplClassName);
         throw new RuntimeException("ClassLoader " + cl + " should not be able to load " + providerImplClassName);
      }
      catch (ClassNotFoundException e)
      {
         //this is expected, just check the class that can't be found is our impl
         if (!(e.getMessage().contains(providerImplClassName)))
         {
            throw new RuntimeException("Unexpected Provider implementation being looked up: " + e.getMessage());
         }
      }
   }
   
   private static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   private static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(classLoader);
               return null;
            }
         });
      }
   }
}
