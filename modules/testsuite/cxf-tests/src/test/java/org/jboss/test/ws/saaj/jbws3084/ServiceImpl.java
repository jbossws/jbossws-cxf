/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.saaj.jbws3084;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

@WebService(portName = "SaajServicePort", serviceName = "SaajService", wsdlLocation = "WEB-INF/wsdl/SaajService.wsdl", targetNamespace = "http://www.jboss.org/jbossws/saaj", endpointInterface = "org.jboss.test.ws.saaj.jbws3084.ServiceIface")
public class ServiceImpl implements ServiceIface
{
   @Resource
   private WebServiceContext context;

   @SuppressWarnings("unchecked")
   public String greetMe()
   {
      try
      {
         Map<String, DataHandler> outDataHandlers = (Map<String, DataHandler>) context.getMessageContext().get(
               MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);

         final char[] content = new char[16 * 1024];
         Arrays.fill(content, 'A');

         DataHandler handler = new DataHandler(new InputStreamDataSource(new ByteArrayInputStream(
               new String(content).getBytes()), "text/plain", "1"));
         outDataHandlers.put("1", handler);
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }

      return "Greetings";
   }
}
