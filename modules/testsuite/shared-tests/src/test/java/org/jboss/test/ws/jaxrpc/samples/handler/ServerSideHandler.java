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
package org.jboss.test.ws.jaxrpc.samples.handler;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;

public class ServerSideHandler extends GenericHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(ServerSideHandler.class);

   protected QName[] headers;
   
   private boolean setCookieOnResponse;

   public QName[] getHeaders()
   {
      return headers;
   }

   public void init(HandlerInfo info)
   {
      log.info("init: " + info);
      headers = info.getHeaders();
      Map configMap = info.getHandlerConfig();
      String value1 = (String)configMap.get("ServerParam1");
      String value2 = (String)configMap.get("ServerParam2");
      if (!"value1".equals(value1) || !"value2".equals(value2))
         throw new IllegalStateException("Invalid handler config: " + configMap);
   }

   public boolean handleRequest(MessageContext msgContext)
   {
      log.info("handleRequest");

      QName[] headers = getHeaders();
      if (headers == null || headers.length != 3)
         throw new IllegalStateException("Invalid number of headers");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();

         SOAPBody soapBody = soapMessage.getSOAPBody();
         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         String rpcName = soapBodyElement.getElementName().getLocalName();

         MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
         String[] cookies = mimeHeaders.getHeader("Cookie");
         if (cookies != null && cookies.length == 1 && cookies[0].equals("client-cookie=true"))
            setCookieOnResponse = true;

         // testInHeader
         if (rpcName.equals("testInHeader"))
         {
            SOAPHeaderElement she = (SOAPHeaderElement)soapHeader.examineAllHeaderElements().next();
            String headerValue = she.getValue();
            if ("IN header message".equals(headerValue) == false)
               throw new JAXRPCException("Unexpected header value: " + headerValue);
         }

         // testOutHeader
         else if (rpcName.equals("testOutHeader"))
         {
            if (soapHeader.examineAllHeaderElements().hasNext())
               throw new JAXRPCException("Unexpected header element");
         }

         // testInOutHeader
         else if (rpcName.equals("testInOutHeader"))
         {
            SOAPHeaderElement she = (SOAPHeaderElement)soapHeader.examineAllHeaderElements().next();
            String headerValue = she.getValue();
            if ("INOUT header message".equals(headerValue) == false)
               throw new JAXRPCException("Unexpected header value: " + headerValue);
         }
         else
         {
            throw new JAXRPCException("Unexpected RPC name: " + rpcName);
         }
      }
      catch (SOAPException e)
      {
         throw new JAXRPCException(e);
      }

      return true;
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      log.info("handleResponse");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();

         SOAPBody soapBody = soapMessage.getSOAPBody();
         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         String rpcName = soapBodyElement.getElementName().getLocalName();

         if (setCookieOnResponse)
         {
            MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
            mimeHeaders.setHeader("Set-Cookie", "server-cookie=true");
            setCookieOnResponse = false;
         }

         if (rpcName.equals("testInHeaderResponse"))
         {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            Name headerName = soapFactory.createName("HeaderValue", "ns2", "http://otherns");
            SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
            she.setValue("Unbound OUT header message");
         }
         else if (rpcName.equals("testOutHeaderResponse"))
         {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            Name headerName = soapFactory.createName("HeaderValue", "ns2", "http://otherns");
            SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
            she.setValue("Unbound OUT header message");
         }
         else if (rpcName.equals("testInOutHeaderResponse"))
         {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            Name headerName = soapFactory.createName("HeaderValue", "ns2", "http://otherns");
            SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
            she.setValue("Unbound OUT header message");
         }
         else
         {
            throw new JAXRPCException("Unexpected RPC name: " + rpcName);
         }
      }
      catch (SOAPException e)
      {
         throw new JAXRPCException(e);
      }

      return true;
   }
}
