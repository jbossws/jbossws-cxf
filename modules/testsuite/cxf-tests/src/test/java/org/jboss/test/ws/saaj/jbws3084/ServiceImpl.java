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
import java.util.Arrays;
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
