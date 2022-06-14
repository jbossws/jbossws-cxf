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

package org.jboss.test.ws.jaxws.jbws2257;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;

@Stateless
@WebService
(
   portName = "AddressingServicePort",
   serviceName = "AddressingService",
   wsdlLocation = "META-INF/wsdl/AddressingService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsaddressing",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws2257.ServiceIface"
)
public class ServiceImpl
{
   private static final Logger log = Logger.getLogger(ServiceImpl.class);
   
   @Resource
   WebServiceContext ctx;
   
   public String sayHello()
   {
      log.info("Current context: " + ctx);
      try
      {
         EndpointReference epr = ctx.getEndpointReference();
         log.info("Endpoint reference: " + epr);
         if (epr == null || !epr.toString().contains("jbws2257"))
         {
            return "Unexpected endpoint reference: " + epr;
         }
      }
      catch (Exception e)
      {
         log.error("Error while reading endpoint reference from context!", e);
         return e.getMessage();
      }
      return "Hello World!";
   }
}
