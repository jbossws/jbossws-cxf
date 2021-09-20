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
