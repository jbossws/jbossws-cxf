/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import jakarta.jws.WebService;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.annotations.Policy;

@WebService
(
   portName = "JavaFirstSecurityServicePort",
   serviceName = "JavaFirstSecurityService",
   name = "JavaFirstServiceIface",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy"
)
@Policy(placement = Policy.Placement.BINDING, uri = "JavaFirstPolicy.xml")
@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServerUsernamePasswordCallback")
      }
)
public class JavaFirstServiceImpl //Not extending JavaFirstServiceIface for testing purposes only, to avoid having to
                                  //move the @Policy annotation in the interface, which is also used on client side. 
{
   public String sayHello()
   {
      return "Secure Hello World!";
   }
}
