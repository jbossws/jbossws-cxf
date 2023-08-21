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
package org.jboss.test.ws.jaxws.jbws2307;

import java.security.Principal;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.servlet.ServletContext;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

@WebService(portName = "HelloPort", serviceName = "HelloService", targetNamespace = "http://helloservice.org/wsdl", wsdlLocation = "WEB-INF/wsdl/HelloService.wsdl", endpointInterface = "org.jboss.test.ws.jaxws.jbws2307.Hello")
public class HelloImpl implements Hello
{

   @Resource
   protected WebServiceContext wsContext;

   public String hello(String s)
   {
      return "Hello, " + s + "!";
   }

   public boolean getServletContextTest()
   {
      if (this.wsContext == null)
         return false;

      ServletContext v = (ServletContext)this.wsContext.getMessageContext().get("jakarta.xml.ws.servlet.context");
      System.out.println("ServletContext=" + v);
      return true;
   }

   public boolean getMessageContextTest()
   {
      if (this.wsContext == null)
         return false;

      MessageContext v = this.wsContext.getMessageContext();
      System.out.println("MessageContext=" + v);
      return true;
   }

   public boolean getUserPrincipalTest()
   {
      if (this.wsContext == null)
         return false;

      Principal v = this.wsContext.getUserPrincipal();
      System.out.println("UserPrincipal=" + v);
      return true;
   }

   public boolean isUserInRoleTest(String s)
   {
      if (this.wsContext == null)
         return false;

      return this.wsContext.isUserInRole(s);
   }
}
