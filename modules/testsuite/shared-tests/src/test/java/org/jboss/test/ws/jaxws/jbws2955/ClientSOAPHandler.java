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
package org.jboss.test.ws.jaxws.jbws2955;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ClientSOAPHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   @Override
   protected boolean handleInbound(final SOAPMessageContext msgContext)
   {
      try
      {
         SOAPMessage message = msgContext.getMessage();

         SOAPBody body = message.getSOAPBody();
         Document document = body.extractContentAsDocument();
         NodeList nodes = document.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Node current = nodes.item(i);

            NodeList childNodes = current.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++)
            {
               Node currentChildNode = childNodes.item(j);
               if ("return".equals(currentChildNode.getLocalName()))
               {
                  currentChildNode.setTextContent("PutByClientSOAPHandler");
               }
            }
         }
         body.addDocument(document);
         message.saveChanges();
      }
      catch (SOAPException e)
      {
         throw new RuntimeException(e);
      }
      return true;
   }
}
