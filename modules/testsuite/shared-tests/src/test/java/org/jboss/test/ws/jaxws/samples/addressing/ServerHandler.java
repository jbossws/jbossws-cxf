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
package org.jboss.test.ws.jaxws.samples.addressing;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext.Scope;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A server side handler for the ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class ServerHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // Provide logging
   private static Logger log = Logger.getLogger(ServerHandler.class);

   private static final QName IDQN = StatefulEndpointImpl.IDQN;

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleRequest");

      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAP addrProps = builder.inboundMap(msgContext);

      if (addrProps == null)
         throw new IllegalStateException("Cannot obtain AddressingProperties");

      String clientid = null;
      MAPEndpoint replyTo = addrProps.getReplyTo();
      for (Object obj :replyTo.getReferenceParameters())
      {
         if (obj instanceof Element)
         {
            Element el = (Element)obj;
            QName qname = getElementQName(el);
            if (qname.equals(IDQN))
            {
               clientid = getTextContent(el);
            }
         }
         else
         {
            log.warn("Unsupported reference parameter found: " + obj);
         }
      }

      if (clientid == null)
         throw new IllegalStateException("Cannot obtain client id");

      // put the clientid in the message context
      msgContext.put("clientid", clientid);
      msgContext.setScope("clientid", Scope.APPLICATION);
      return true;
   }

   @Override
   public boolean handleOutbound(SOAPMessageContext msgContext)
   {
      log.info("handleResponse");

      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAP inProps = builder.inboundMap(msgContext);
      MAP outProps = builder.newMap();
      outProps.initializeAsDestination(inProps.getReplyTo());

      outProps.installOutboundMapOnServerSide(msgContext, outProps);
      msgContext.setScope(builder.newConstants().getServerAddressingPropertiesOutbound(), Scope.APPLICATION);

      return true;
   }

   // ---------------- DOM Util methods -------------------

   /** Get the qname of the given node.
    */
   protected static QName getElementQName(Element el)
   {
      String qualifiedName = el.getNodeName();
      return resolveQName(el, qualifiedName);
   }

   /** Transform the given qualified name into a QName
    */
   protected static QName resolveQName(Element el, String qualifiedName)
   {
      QName qname;
      String prefix = "";
      String namespaceURI = "";
      String localPart = qualifiedName;

      int colIndex = qualifiedName.indexOf(":");
      if (colIndex > 0)
      {
         prefix = qualifiedName.substring(0, colIndex);
         localPart = qualifiedName.substring(colIndex + 1);

         if ("xmlns".equals(prefix))
         {
            namespaceURI = "URI:XML_PREDEFINED_NAMESPACE";
         }
         else
         {
            Element nsElement = el;
            while (namespaceURI.equals("") && nsElement != null)
            {
               namespaceURI = nsElement.getAttribute("xmlns:" + prefix);
               if (namespaceURI.equals(""))
                  nsElement = getParentElement(nsElement);
            }
         }

         if (namespaceURI.equals("") && el.getNamespaceURI() != null)
         {
            namespaceURI = el.getNamespaceURI();
         }

         if (namespaceURI.equals(""))
            throw new IllegalArgumentException("Cannot find namespace uri for: " + qualifiedName);
      }
      else
      {
         Element nsElement = el;
         while (namespaceURI.equals("") && nsElement != null)
         {
            namespaceURI = nsElement.getAttribute("xmlns");
            if (namespaceURI.equals(""))
               nsElement = getParentElement(nsElement);
         }
      }

      qname = new QName(namespaceURI, localPart, prefix);
      return qname;
   }

   /** Gets parent element or null if there is none
    */
   protected static Element getParentElement(Node node)
   {
      Node parent = node.getParentNode();
      return (parent instanceof Element ? (Element)parent : null);
   }

   /** Get the concatenated text content, or null.
    */
   protected static String getTextContent(Node node)
   {
      boolean hasTextContent = false;
      StringBuilder buffer = new StringBuilder();
      NodeList nlist = node.getChildNodes();
      int len = nlist.getLength();
      for (int i = 0; i < len; i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.TEXT_NODE)
         {
            buffer.append(child.getNodeValue());
            hasTextContent = true;
         }
      }
      return (hasTextContent ? buffer.toString() : null);
   }
}
