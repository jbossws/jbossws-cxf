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
package org.jboss.test.ws.jaxws.binding;

import static javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.jboss.logging.Logger;

@WebService(name="SOAPEndpoint", targetNamespace="http://org.jboss.ws/jaxws/binding", 
      portName="SOAPEndpointPort",
      serviceName="SOAPEndpointService", 
      endpointInterface = "org.jboss.test.ws.jaxws.binding.SOAPEndpoint")
@BindingType(SOAP12HTTP_BINDING)
@HandlerChain(file = "jaxws-server-handlers.xml")
public class SOAPEndpointBean implements SOAPEndpoint
{
   private static Logger log = Logger.getLogger(SOAPEndpointBean.class);

   public String namespace()
   {
      //Get the nsUri in the received message that was analyzed in the server handler
      //just a trick for this test, not to be used with real world apps 
      String nsURI = ServerHandler.getNsURI();

      log.info(nsURI);

      return nsURI;
   }
}
