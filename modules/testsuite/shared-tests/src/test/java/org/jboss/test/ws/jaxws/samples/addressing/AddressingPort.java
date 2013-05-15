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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.DOMUtils;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import org.jboss.ws.api.addressing.MAPConstants;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.common.utils.UUIDGenerator;
import org.w3c.dom.Element;

/**
 * This is a wrapper around the StatefulEndpoint port, setting / handling client IDs
 * using WS-Addressing (common JSR-261 API).
 * 
 * The work performed by this class cannot always be done simply using
 * handlers on client side. That's because (for instance) CXF's
 * ws-addressing interceptors run before user provided handlers
 * when dealing with outbound client messages.
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-May-2009
 *
 */
public class AddressingPort implements StatefulEndpoint
{
   private static Logger log = Logger.getLogger(AddressingPort.class);
   private static final QName IDQN = StatefulEndpointImpl.IDQN;
   private StatefulEndpoint port;
   private static int maxClientId;
   private String clientID;
   
   public AddressingPort(StatefulEndpoint port)
   {
      this.port = port;
   }
   
   
   public void addItem(String item) throws RemoteException
   {
      setClientID();
      port.addItem(item);
      readClientID();
   }

   public void checkout() throws RemoteException
   {
      setClientID();
      port.checkout();
      readClientID();
   }

   public String getItems() throws RemoteException
   {
      setClientID();
      String result = port.getItems();
      readClientID();
      return result;
   }
   
   /**
    * This installs the ID for this client in the outbound messages
    */
   private void setClientID()
   {
      BindingProvider bindingProvider = (BindingProvider)port;
      Map<String, Object> msgContext = bindingProvider.getRequestContext();
      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAPConstants ADDR = builder.newConstants();
      MAP outProps = builder.newMap();
      MAPEndpoint replyTo = builder.newEndpoint(ADDR.getAnonymousURI());
      outProps.setReplyTo(replyTo);
      outProps.setMessageID("urn:uuid:" + UUIDGenerator.generateRandomUUIDString());
      // Assign a new clientid
      if (clientID == null)
      {
         clientID = "clientid-" + (++maxClientId);
         log.info("New clientid: " + clientID);
      }
      try
      {
         replyTo.addReferenceParameter(DOMUtils.parse(getClientIdElement(clientID), getDocumentBuilder()));
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      outProps.installOutboundMapOnClientSide(msgContext, outProps);
   }

   /**
    * This verifies the client ID is available in inbound messages
    */
   @SuppressWarnings("unchecked")
   private void readClientID()
   {
      BindingProvider bindingProvider = (BindingProvider)port;
      Map<String, Object> msgContext = bindingProvider.getResponseContext();
      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAP addrProps = builder.inboundMap(msgContext);
      if (addrProps == null)
         throw new IllegalStateException("Cannot obtain AddressingProperties");
      for (Object obj : addrProps.getReferenceParameters())
      {
         if (obj instanceof Element) //Native always uses Element for ref params 
         {
            Element el = (Element)obj;
            QName qname = DOMUtils.getElementQName(el);
            if (qname.equals(IDQN))
            {
               clientID = DOMUtils.getTextContent(el);
            }
         }
         else if (obj instanceof JAXBElement) //CXF also uses JAXBElement
         {
            JAXBElement<String> el = (JAXBElement<String>)obj;
            if (IDQN.equals(el.getName()))
            {
               clientID = el.getValue();
            }
         }
      }
      if (clientID == null)
         throw new IllegalStateException("Cannot obtain clientid");
   }
   
   private static String getClientIdElement(String clientid)
   {
      String qualname = IDQN.getPrefix() + ":" + IDQN.getLocalPart();
      StringBuffer buffer = new StringBuffer("<" + qualname);
      buffer.append(" xmlns:" + IDQN.getPrefix() + "='" + IDQN.getNamespaceURI() + "'");
      buffer.append(">" + clientid + "</" + qualname + ">");
      return buffer.toString();
   }
   
   private static DocumentBuilder getDocumentBuilder()
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
