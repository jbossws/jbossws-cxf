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
package org.jboss.test.ws.jaxrpc.samples.message;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.DOMUtils;
import org.jboss.ws.common.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 26-Nov-2004
 */
public class MessageTestServiceBean implements MessageTestService
{
   // provide logging
   private final Logger log = Logger.getLogger(MessageTestServiceBean.class);

   /** org.w3c.dom.Element
    */
   public SOAPElement processElement(SOAPElement msg) throws RemoteException
   {
      String msgStr = DOMWriter.printNode(msg, true);
      log.info("processElement: " + msgStr);

      try
      {
         Element reqEl = (Element)msg;

         // verify order element
         QName qname = new QName(TARGET_NAMESPACE, "Order", PREFIX_1);
         QName elementName = new QName(reqEl.getNamespaceURI(), reqEl.getLocalName(), reqEl.getPrefix());
         if (qname.equals(elementName) == false)
            throw new IllegalArgumentException("Unexpected element: " + elementName);

         // Verify the custom attribute
         String attrVal = reqEl.getAttribute("attrval");
         if ("somevalue".equals(attrVal) == false)
            throw new IllegalArgumentException("Unexpected attribute value: " + attrVal);

         // Verify NS declarations
         String nsURI_1 = reqEl.getAttribute("xmlns:" + PREFIX_1);
         if (TARGET_NAMESPACE.equals(nsURI_1) == false)
            throw new IllegalArgumentException("Unexpected namespace URI: " + nsURI_1);

         String nsURI_2 = reqEl.getAttribute("xmlns:" + PREFIX_2);
         if (NSURI_2.equals(nsURI_2) == false)
            throw new IllegalArgumentException("Unexpected namespace URI: " + nsURI_2);

         // Test getElementsByTagNameNS
         // http://jira.jboss.com/jira/browse/JBWS-99
         NodeList nodeList1 = reqEl.getElementsByTagNameNS(NSURI_2, "Customer");
         if (nodeList1.getLength() != 1)
            throw new IllegalArgumentException("Cannot getElementsByTagNameNS");

         // Test getElementsByTagName
         // http://jira.jboss.com/jira/browse/JBWS-99
         NodeList nodeList2 = reqEl.getElementsByTagName("Item");
         if (nodeList2.getLength() != 1)
            throw new IllegalArgumentException("Cannot getElementsByTagName");

         // verify customer element
         qname = new QName(NSURI_2, "Customer", PREFIX_2);
         Element custEl = DOMUtils.getFirstChildElement(reqEl, qname);
         String elementValue = DOMUtils.getTextContent(custEl);
         if ("Customer".equals(custEl.getLocalName()) == false || "Kermit".equals(elementValue) == false)
            throw new IllegalArgumentException("Unexpected element value: " + elementValue);

         // verify item element
         qname = new QName("Item");
         Element itemEl = DOMUtils.getFirstChildElement(reqEl, qname);
         elementValue = DOMUtils.getTextContent(itemEl);
         if ("Item".equals(itemEl.getLocalName()) == false || "Ferrari".equals(elementValue) == false)
            throw new IllegalArgumentException("Unexpected element value: " + elementValue);

         // Setup document builder
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         docBuilderFactory.setNamespaceAware(true);

         // Prepare response
         DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));

         SOAPElement parent = SOAPFactory.newInstance().createElement("dummy");
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer();
         transformer.transform(new DOMSource(doc), new DOMResult(parent));
         return (SOAPElement)parent.getChildElements().next();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RemoteException(e.toString(), e);
      }
   }
}
