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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.LogicalMessageContext;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericLogicalHandler;
import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Element;

/**
 * A jaxws logical handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class LogicalSourceHandler extends GenericLogicalHandler<LogicalMessageContext>
{
   // provide logging
   private static final Logger log = Logger.getLogger(LogicalSourceHandler.class);

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

   private boolean appendHandlerName(MessageContext msgContext, String direction)
   {
      try
      {
         // Get the payload as Source
         LogicalMessageContext logicalContext = (LogicalMessageContext)msgContext;
         Source source = logicalContext.getMessage().getPayload();
         TransformerFactory tf = TransformerFactory.newInstance();
         ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
         tf.newTransformer().transform(source, new StreamResult(baos));

         // Parse the payload and extract the value
         Element root = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()), getDocumentBuilder());
         Element element = DOMUtils.getFirstChildElement(root);

         String oldValue = DOMUtils.getTextContent(element);
         String newValue = oldValue + ":" + direction + ":LogicalSourceHandler";
         element.setTextContent(newValue);

         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);

         // Set the updated payload
         source = new DOMSource(root);
         logicalContext.getMessage().setPayload(source);

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

   private DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilderFactory factory = null;
      try
      {
         factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setExpandEntityReferences(false);
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create document builder", e);
      }
   }
}
