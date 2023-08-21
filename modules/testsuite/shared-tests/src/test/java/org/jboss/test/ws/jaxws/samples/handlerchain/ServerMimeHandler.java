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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A server side handler
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 08-Oct-2005
 */
public class ServerMimeHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static Logger log = Logger.getLogger(ServerMimeHandler.class);

   private boolean setCookieOnResponse;

   @Override
   protected boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      // legacy JBossWS-Native approach...
      SOAPMessage soapMessage = msgContext.getMessage();
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      String[] cookies = mimeHeaders.getHeader("Cookie");

      // proper approach through MessageContext.HTTP_REQUEST_HEADERS
      if (cookies == null) {
         @SuppressWarnings("unchecked")
         Map<String, List<String>> httpHeaders = (Map<String, List<String>>) msgContext.get(MessageContext.HTTP_REQUEST_HEADERS);
         List<String> l = httpHeaders.get("Cookie");
         if (l != null && !l.isEmpty()) {
            cookies = l.toArray(new String[l.size()]);
         }
      }

      if (cookies != null && cookies.length == 1 && cookies[0].equals("client-cookie=true"))
         setCookieOnResponse = true;

      return true;
   }


   @Override
   protected boolean handleOutbound(SOAPMessageContext msgContext)
   {
      log.info("handleOutbound");

      if (setCookieOnResponse)
      {
         // legacy JBossWS-Native approach
         SOAPMessage soapMessage = msgContext.getMessage();
         MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
         mimeHeaders.setHeader("Set-Cookie", "server-cookie=true");

         // proper approach through MessageContext.HTTP_REQUEST_HEADERS
         Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
         httpHeaders.put("Set-Cookie", Collections.singletonList("server-cookie=true"));
         msgContext.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);

         setCookieOnResponse = false;
      }

      return true;
   }
}
