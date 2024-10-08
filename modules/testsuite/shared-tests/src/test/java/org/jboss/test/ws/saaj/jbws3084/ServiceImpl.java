/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

import jakarta.activation.DataHandler;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

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

      boolean chunkedDisabled = reqHeaders.get("Content-Length") != null;

      if (chunkedDisabled) {
         Map<String, List<String>> respHeaders = (Map<String, List<String>>) context.getMessageContext().get(
                 MessageContext.HTTP_RESPONSE_HEADERS);
         if (respHeaders == null)
         {
            respHeaders = new HashMap<String, List<String>>();
            context.getMessageContext().put(MessageContext.HTTP_RESPONSE_HEADERS, respHeaders);
         }
         respHeaders.put("Transfer-Encoding", Arrays.asList("disabled"));
      }

      /*
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
      */
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
