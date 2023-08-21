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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.LogicalMessageContext;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.ws.api.handler.GenericLogicalHandler;

public class LogicalJAXBHandler extends GenericLogicalHandler<LogicalMessageContext>
{
   @Override
   public boolean handleOutbound(LogicalMessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(LogicalMessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Inbound");
   }

   @SuppressWarnings("unchecked")
   private boolean appendHandlerName(MessageContext msgContext, String direction)
   {
      try
      {
         // Get the payload as Source
         LogicalMessageContext logicalContext = (LogicalMessageContext)msgContext;
         JAXBContext jaxb = JAXBContext.newInstance(Echo.class.getPackage().getName());
         Object payload = logicalContext.getMessage().getPayload(jaxb);

         JAXBElement<Object> jaxbElement = null;
         if (payload instanceof JAXBElement)
         {
            jaxbElement = (JAXBElement<Object>)payload;
            payload = jaxbElement.getValue();
         }

         if (payload instanceof Echo)
         {
            Echo echo = (Echo)payload;
            String value = echo.getString1();
            echo.setString1(value + ":" + direction + ":LogicalJAXBHandler");
         }
         else if (payload instanceof EchoResponse)
         {
            EchoResponse echo = (EchoResponse)payload;
            String value = echo.getResult();
            echo.setResult(value + ":" + direction + ":LogicalJAXBHandler");
         }
         else
         {
            throw new WebServiceException("Invalid payload type: " + payload);
         }

         if (jaxbElement != null)
         {
            jaxbElement.setValue(payload);
            payload = jaxbElement;
         }

         // Set the updated payload
         logicalContext.getMessage().setPayload(payload, jaxb);

         return true;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
