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
package org.jboss.test.ws.jaxws.jbws1611;

import javax.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;

@WebService(name = "PingEndpoint", serviceName = "PingEndpointService")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class PingEndpointImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(PingEndpointImpl.class);

   @Resource
   WebServiceContext wsctx;

   @Action(input="uri:placeBuyOrder")
   @WebMethod
   public String ping()
   {
      HttpServletRequest req = (HttpServletRequest)wsctx.getMessageContext().get(MessageContext.SERVLET_REQUEST);
      String soapAction = req.getHeader("SOAPAction");
      log.info("ping: " + soapAction);
      return soapAction;
   }
}
