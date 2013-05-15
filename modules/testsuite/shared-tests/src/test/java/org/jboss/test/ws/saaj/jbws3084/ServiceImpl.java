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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
   public String sayHello()
   {
      Map<String, List<String>> reqHeaders = (Map<String, List<String>>) context.getMessageContext().get(
            MessageContext.HTTP_REQUEST_HEADERS);

      boolean chunkedEncodingDisabled = reqHeaders.get("transfer-encoding-disabled") != null;

      List<String> transferEncHeader = reqHeaders.get("transfer-encoding");

      if (!chunkedEncodingDisabled)
      {
         if (transferEncHeader == null || transferEncHeader.size() != 1)
         {
            throw new RuntimeException("Transfer-Encoding is missing");
         }
         if (!"chunked".equals(transferEncHeader.get(0)))
         {
            throw new RuntimeException("Wrong Transfer-Encoding value");
         }
      }
      else
      {
         if (transferEncHeader != null)
         {
            throw new RuntimeException("Unexpected Transfer-Encoding header");
         }
         Map<String, List<String>> respHeaders = (Map<String, List<String>>) context.getMessageContext().get(
               MessageContext.HTTP_RESPONSE_HEADERS);
         if (respHeaders == null)
         {
            respHeaders = new HashMap<String, List<String>>();
            context.getMessageContext().put(MessageContext.HTTP_RESPONSE_HEADERS, respHeaders);
         }
         respHeaders.put("Transfer-Encoding-Disabled", Arrays.asList("true"));
      }

      Map<String, DataHandler> dataHandlers = (Map<String, DataHandler>) context.getMessageContext().get(
            MessageContext.INBOUND_MESSAGE_ATTACHMENTS);

      Map<String, DataHandler> outDataHandlers = (Map<String, DataHandler>) context.getMessageContext().get(
            MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);

      int index = 0;
      try
      {
         for (Map.Entry<String, DataHandler> entry : dataHandlers.entrySet())
         {
            InputStream is = entry.getValue().getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(baos, is);
            String name = Integer.toString(index++);
            DataHandler handler = new DataHandler(new InputStreamDataSource(
                  new ByteArrayInputStream(baos.toByteArray()), "text/plain", name));
            outDataHandlers.put(name, handler);
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }

      return "Hello World!";
   }
   
   public static void copyStream(OutputStream outs, InputStream ins) throws IOException
   {
      try
      {
         byte[] bytes = new byte[1024];
         int r = ins.read(bytes);
         while (r > 0)
         {
            outs.write(bytes, 0, r);
            r = ins.read(bytes);
         }
      }
      catch (IOException e)
      {
         throw e;
      }
      finally{
         ins.close();
      }
   }
}
