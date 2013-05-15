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
package org.jboss.test.ws.publish;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;

@WebService(serviceName="EndpointService", portName="EndpointPort", endpointInterface = "org.jboss.test.ws.publish.Endpoint")
public class EndpointImpl
{
   @Resource
   WebServiceContext wsCtx;
   
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl.class);

   public String echo(String input)
   {
      log.info("echo: " + input);
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      if (msgContext == null) {
         return "MessageContext is null!";
      }
      log.info("WSDL_DESCRIPTION: " + msgContext.get(MessageContext.WSDL_DESCRIPTION));
      log.info("WSDL_SERVICE: " + msgContext.get(MessageContext.WSDL_SERVICE));
      log.info("WSDL_INTERFACE: " + msgContext.get(MessageContext.WSDL_INTERFACE));
      log.info("WSDL_PORT: " + msgContext.get(MessageContext.WSDL_PORT));
      log.info("WSDL_OPERATION: " + msgContext.get(MessageContext.WSDL_OPERATION));
      return input;
   }
}
