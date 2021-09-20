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
package org.jboss.test.ws.jaxws.jbws3034;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServerSOAPHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   @Override
   protected boolean handleOutbound(final SOAPMessageContext msgContext)
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
                  currentChildNode.setTextContent("PutByServerSOAPHandler");
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
