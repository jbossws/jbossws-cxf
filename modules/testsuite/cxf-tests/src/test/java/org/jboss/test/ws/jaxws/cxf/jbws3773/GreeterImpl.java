/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3773;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.Addressing;

@WebService(serviceName = "SOAPService", portName = "SoapPort", 
            endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3773.Greeter", 
            targetNamespace = "http://jboss.org/hello_world")
@Addressing
public class GreeterImpl implements Greeter
{
   @Resource
   private WebServiceContext webServiceCtx;
   
   public String sayHi(String request)
   {
      MessageContext ctx = webServiceCtx.getMessageContext();
      HttpServletRequest hsr = (HttpServletRequest)ctx.get(MessageContext.SERVLET_REQUEST);
      String scheme = hsr.getScheme();
      return scheme;
   }
}
