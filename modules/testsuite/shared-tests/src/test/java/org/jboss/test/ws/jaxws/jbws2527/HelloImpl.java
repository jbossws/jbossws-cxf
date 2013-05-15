/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2527;

import java.security.Principal;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

@WebService(portName = "HelloPort", serviceName = "HelloService", targetNamespace = "http://helloservice.org/wsdl", wsdlLocation = "WEB-INF/wsdl/HelloService.wsdl", endpointInterface = "org.jboss.test.ws.jaxws.jbws2527.Hello")
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

      ServletContext v = (ServletContext)this.wsContext.getMessageContext().get("javax.xml.ws.servlet.context");
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
