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
package org.jboss.test.ws.jaxws.jbws3401;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService(name = "TestEndpoint", serviceName="TestEndpointService", targetNamespace = "http://org.jboss.test.ws/jbws3401", wsdlLocation="WEB-INF/wsdl/TestService.wsdl")
@Stateless
public class TestEndpointImpl implements TestEndpoint
{

   public String echo(String message)
   {
      return message;
   }

}
