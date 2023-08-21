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
package org.jboss.test.ws.saaj.jbws3857;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class SoapMultipartCheckerServlet extends HttpServlet {

   @Override
   protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
      final String requestedContentType = req.getHeader("Content-Type");
      System.out.println("Requested content type : " + requestedContentType);

      try {
         replyRequestedContentTypeInResponse(requestedContentType, resp);
      } catch (final Exception e) {
         throw new ServletException(e.getMessage(), e);
      }
   }

   private void replyRequestedContentTypeInResponse(final String requestedContentType, final HttpServletResponse resp) throws Exception {
      final MessageFactory msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
      final SOAPMessage msg = msgFactory.createMessage();
      final SOAPBodyElement bodyElement = msg.getSOAPBody().addBodyElement(
         new QName("urn:not-important:1.0", "requestedContentType"));

      bodyElement.addTextNode(requestedContentType);

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setHeader("Content-Type", "application/soap+xml; charset=utf-8");
      final OutputStream os = resp.getOutputStream();
      msg.writeTo(os);
      os.close();
   }
}
