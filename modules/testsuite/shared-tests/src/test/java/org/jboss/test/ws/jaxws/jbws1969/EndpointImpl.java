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
package org.jboss.test.ws.jaxws.jbws1969;

import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

import org.jboss.ws.api.annotation.WebContext;

/**
 * The SEI implementation
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 */
@Stateless
@WebService
(
   name = "Endpoint", 
   serviceName = "EndpointService",
   wsdlLocation = "/META-INF/wsdl/echo/TestService.wsdl",
   targetNamespace = "http://org.jboss.ws/jbws1969"
)
@WebContext(contextRoot="jaxws-jbws1969", urlPattern="/*")
public class EndpointImpl implements Endpoint
{
   @WebMethod
   public String echo(String input)
   {
      return input;
   }
}
