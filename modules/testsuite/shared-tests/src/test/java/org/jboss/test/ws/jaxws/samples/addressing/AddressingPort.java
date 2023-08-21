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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.ws.BindingProvider;

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
