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
package org.jboss.test.ws.jaxws.webfault;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test the JSR-181 annotation: javax.jws.WebFault
 *
 * @author alessio.soldano@jboss.org
 * @since 21-Feb-2008
 */
public class WebFaultTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-webfault";
   private static final String TARGET_NS = "http://webfault.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(WebFaultTestCase.class, "jaxws-webfault.war");
   }
   
   /**
    * Tests whether the @WebFault annotation correctly sets the fault element's name and namespace
    * (the type doesn't depend on @WebFault, see [JBWS-1904] about this)
    * 
    * Also tests that the @XmlTransient annotated fields are not included in the wsdl [JBWS-2152].
    * 
    * @throws Exception
    */
   public void testWebFaultElement() throws Exception
   {
      if (isIntegrationCXF())
      {
         System.out.println("FIXME: [CXF-1519] Explicitely set the namespace of a WebFault");
         return;
      }
      
      Document doc = DOMUtils.getDocumentBuilder().parse(new URL(endpointURL + "?wsdl").toString());
      NodeList schemas = ((Element)doc.getDocumentElement()
         .getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "types").item(0))
            .getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");

      boolean firstElementFound = false;
      boolean secondElementFound = false;
      boolean firstTypeFound = false;
      boolean secondTypeFound = false;
      for (int i = 0; i < schemas.getLength(); i++)
      {
         Element schema = (Element)schemas.item(i);
         if (schema.getAttribute("targetNamespace").equals("org.jboss.test.ws.jaxws.webfault.exceptions"))
         {
            Iterator<Element> elements = DOMUtils.getChildElements(schema, new QName("http://www.w3.org/2001/XMLSchema", "element"));
            while (elements.hasNext())
            {
               Element e = (Element)elements.next();
               boolean nameEquals = e.getAttribute("name").equals("myCustomFault");
               boolean typeEquals = e.getAttribute("type").endsWith(":CustomException");
               if (nameEquals && typeEquals)
               {
                  firstElementFound = true;
               }
            }
         }
         if (schema.getAttribute("targetNamespace").equals("http://webfault.jaxws.ws.test.jboss.org/"))
         {
            Iterator<Element> elements = DOMUtils.getChildElements(schema, new QName("http://www.w3.org/2001/XMLSchema", "element"));
            while (elements.hasNext())
            {
               Element e = (Element)elements.next();
               boolean nameEquals = e.getAttribute("name").equals("SimpleException");
               boolean typeEquals = e.getAttribute("type").endsWith(":SimpleException");
               if (nameEquals && typeEquals)
               {
                  secondElementFound = true;
               }
            }
            elements = DOMUtils.getChildElements(schema, new QName("http://www.w3.org/2001/XMLSchema", "complexType"));
            while (elements.hasNext())
            {
               Element e = (Element)elements.next();
               boolean nameEquals = e.getAttribute("name").equals("CustomException");
               if (nameEquals)
               {
                  firstTypeFound = true;
                  
                  Element sequence = (Element)DOMUtils.getChildElements(e, new QName("http://www.w3.org/2001/XMLSchema", "sequence")).next();
                  Iterator<Element> children = (Iterator<Element>)DOMUtils.getChildElements(sequence, new QName("http://www.w3.org/2001/XMLSchema", "element"));
                  boolean numberAttributeFound = false;
                  while (children.hasNext())
                  {
                     String name = children.next().getAttribute("name");
                     if ("number".equals(name))
                        numberAttributeFound = true;
                     else if ("transientString".equalsIgnoreCase(name))
                        fail("@XmlTransient fields should not be included.");
                  }
                  assertTrue(numberAttributeFound);
               }
               nameEquals = e.getAttribute("name").equals("SimpleException");
               if (nameEquals)
               {
                  secondTypeFound = true;
               }
            }
         }
      }
      assertTrue(firstElementFound && secondElementFound);
      assertTrue(firstTypeFound && secondTypeFound);
   }
   
   public void testInvocation() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(TARGET_NS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      try
      {
         port.throwCustomException("Hello");
         fail("Exception expected!");
      }
      catch (CustomException e)
      {
         assertEquals(Integer.valueOf(5), e.getNumber());
         assertNull(e.getTransientString());
      }
      catch (Exception e)
      {
         fail("Wrong exception caught!");
      }
      try
      {
         port.throwSimpleException("World");
         fail("Exception expected!");
      }
      catch (SimpleException e)
      {
         assertEquals(Integer.valueOf(5), e.getNumber());
      }
      catch (Exception e)
      {
         fail("Wrong exception caught!");
      }
   }
}
