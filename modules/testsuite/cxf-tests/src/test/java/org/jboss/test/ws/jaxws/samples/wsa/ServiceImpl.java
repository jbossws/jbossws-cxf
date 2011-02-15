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
package org.jboss.test.ws.jaxws.samples.wsa;

import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

import org.jboss.logging.Logger;

@WebService
(
   portName = "AddressingServicePort",
   serviceName = "AddressingService",
//   wsdlLocation = "WEB-INF/wsdl/AddressingService.wsdl", //do not provide the wsdl and let the endpoint publish the proper one with addressing policy
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsaddressing",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsa.ServiceIface"
)
@Addressing(enabled=true, required=true)
public class ServiceImpl implements ServiceIface
{
   private Logger log = Logger.getLogger(this.getClass());
   
   public String sayHello(String name)
   {
      if ("Sleepy".equals(name))
      {
         try
         {
            log.info("Sleeping...");
            Thread.sleep(30 * 1000);
            log.info("...end of sleeping.");
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      return "Hello " + name + "!";
   }
}
