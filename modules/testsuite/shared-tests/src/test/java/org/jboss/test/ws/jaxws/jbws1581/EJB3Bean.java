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
package org.jboss.test.ws.jaxws.jbws1581;

import java.net.URL;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;

@Stateless
@Remote(EJB3Remote.class)
public class EJB3Bean implements EJB3Remote
{
   @WebServiceRef(value = EndpointService.class, wsdlLocation = "META-INF/wsdl/TestService.wsdl")
   public EndpointInterface port;

   public String runTest(String message)
   {
      URL seiSource = EndpointInterface.class.getProtectionDomain().getCodeSource().getLocation();
      URL ejb3Source = EJB3Bean.class.getProtectionDomain().getCodeSource().getLocation();

      if (seiSource.equals(ejb3Source) == false)
         message = "SEI incorrectly loaded from: " + seiSource;

      return port.hello(message);
   }

}
