/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3250;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;


@WebService(
      name = "TestEndpoint",
      targetNamespace = "http://ws.jboss.org/jbws3250",
      serviceName = "TestEndpointService", 
      endpointInterface = "org.jboss.test.ws.jaxws.jbws3250.Endpoint")
@SOAPBinding(style = SOAPBinding.Style.RPC,
       use = SOAPBinding.Use.LITERAL)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")

public class EndpointImpl implements Endpoint
{
   public MTOMResponse echo(MTOMRequest message) {
      MTOMResponse response = new MTOMResponse();
      response.setContent(message.getContent());
      response.setResponse("Response for requestID:" + message.getId());
      return response;
   }
}
