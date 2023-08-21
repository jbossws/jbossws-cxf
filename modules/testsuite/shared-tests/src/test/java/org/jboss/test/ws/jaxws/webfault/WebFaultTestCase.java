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
package org.jboss.test.ws.jaxws.webfault;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebFault
 *
 * @author alessio.soldano@jboss.org
 * @since 21-Feb-2008
 */
@RunWith(Arquillian.class)
public class WebFaultTestCase extends JBossWSTest
{
   private static final String TARGET_NS = "http://webfault.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-webfault.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.webfault.CustomException.class)
               .addClass(org.jboss.test.ws.jaxws.webfault.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.webfault.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.webfault.SimpleException.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/webfault/WEB-INF/web.xml"));
      return archive;
   }

   /**
    * Tests whether the @WebFault annotation correctly sets the fault element's name and namespace
    * (the type doesn't depend on @WebFault, see [JBWS-1904] about this)
    * 
    * Also tests that the @XmlTransient annotated fields are not included in the wsdl [JBWS-2152].
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testWebFaultElement() throws Exception
   {
      if (isIntegrationCXF())
      {
         System.out.println("FIXME: [CXF-1519] Explicitely set the namespace of a WebFault");
         return;
      }
      
      Document doc = DOMUtils.getDocumentBuilder().parse(new URL(baseURL + "/jaxws-webfault?wsdl").toString());
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

   @Test
   @RunAsClient
   public void testInvocation() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-webfault?wsdl");
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
