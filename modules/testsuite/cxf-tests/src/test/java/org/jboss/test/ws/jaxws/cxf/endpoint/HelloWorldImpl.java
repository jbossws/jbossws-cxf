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
package org.jboss.test.ws.jaxws.cxf.endpoint;

import jakarta.jws.WebService;

@WebService(serviceName = "HelloWorldService", endpointInterface = "org.jboss.test.ws.jaxws.cxf.endpoint.HelloWorld", targetNamespace = "http://org.jboss.ws/jaxws/cxf/endpoint")
public class HelloWorldImpl implements HelloWorld
{
   private ClassLoader _deploymentClassLoader;
   
   public HelloWorldImpl() {}

   public HelloWorldImpl(ClassLoader classloader)
   {
      _deploymentClassLoader = classloader;
   }

   public String getClassLoader()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      //JBWS-3223
      //use check on class to avoid dependency declaration in MANIFEST for AS7
      if (cl.getClass().getName().equals("org.jboss.ws.common.utils.DelegateClassLoader")) {
         cl = cl.getParent();
      }
      return cl.toString() + cl.hashCode();
   }

   public String getDeploymentClassLoader()
   {
      return _deploymentClassLoader.toString() + _deploymentClassLoader.hashCode();
   }
}
