/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3655;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import org.jboss.ws.api.annotation.WebContext;

@Stateless
@WebService(wsdlLocation = "META-INF/wsdl/Hello.wsdl", 
name = HelloWs.NAME, 
serviceName = HelloWs.NAME,
targetNamespace = HelloWs.TARGET_NAMESPACE, 
endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWs")
@WebContext(contextRoot = "/jaxws-cxf-jbws3655", urlPattern = HelloWs.NAME)
public class HelloWSEJBImpl implements HelloWs
{

   public HelloResponse doHello(HelloRequest request)
   {
      HelloResponse response = new HelloResponse();
      response.getMultiHello().add(request.getInput());
      response.getMultiHello().add("world");
      return response;
   }

}
