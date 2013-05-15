/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.jbws3293;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.test.helper.DOMWriter;
import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOAPHandler extends GenericSOAPHandler<LogicalMessageContext>
{

   private static final Logger log = Logger.getLogger(SOAPHandler.class);

   @Override
   protected boolean handleInbound(final MessageContext msgContext)
   {
      log.info("handleInbound()");

      try
      {
         SOAPMessageContext smc = (SOAPMessageContext)msgContext;
         SOAPMessage message = smc.getMessage();

         SOAPBody body = message.getSOAPBody();
         Document document = body.extractContentAsDocument();
         Node node = document;

         log.info(DOMWriter.printNode(node, true));

         NodeList nodes = node.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Node current = nodes.item(i);

            NodeList childNodes = current.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++)
            {
               Node currentChildNode = childNodes.item(j);
               if ("result".equals(currentChildNode.getLocalName()))
               {
                  currentChildNode.setTextContent(currentChildNode.getTextContent() + " modified in handler");
               }
            }
         }

         log.info(DOMWriter.printNode(node, true));

         // Add document back as removed by call to 'extractContentAsDocument()'
         body.addDocument(document);
         message.saveChanges();
      }
      catch (SOAPException e)
      {
         throw new RuntimeException("Error in Handler", e);
      }

      log.info("Finished");
      return true;
   }

}
