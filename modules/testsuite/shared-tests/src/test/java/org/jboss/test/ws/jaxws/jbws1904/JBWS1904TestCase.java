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
package org.jboss.test.ws.jaxws.jbws1904;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * [JBWS-1904] Explicitly set the namespace of a WebFault
 *
 * http://jira.jboss.org/jira/browse/JBWS-1904
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Dec-2007
 */
@RunWith(Arquillian.class)
public class JBWS1904TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1904.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1904.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1904.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1904.UserException.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLSchema() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1904?wsdl");

      Document doc = DOMUtils.getDocumentBuilder().parse(wsdlURL.toString());
      NodeList schemas = ((Element)doc.getDocumentElement()
         .getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "types").item(0))
            .getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");

      boolean firstTypeFound = false;
      boolean secondTypeFound = false;
      for (int i = 0; i < schemas.getLength(); i++)
      {
         Element schema = (Element)schemas.item(i);
         if (schema.getAttribute("targetNamespace").equals("http://org.jboss.ws/jbws1904/faults"))
         {
            Iterator<?> elements = DOMUtils.getChildElements(schema, new QName("http://www.w3.org/2001/XMLSchema", "element"));
            while (elements.hasNext())
            {
               Element e = (Element)elements.next();
               boolean nameEquals = e.getAttribute("name").equals("UserExceptionFault");
               boolean typeEquals = e.getAttribute("type").endsWith(":UserException");
               if (nameEquals && typeEquals)
               {
                  firstTypeFound = true;
               }
            }
         }
         if (schema.getAttribute("targetNamespace").equals("http://org.jboss.ws/jbws1904/exceptions"))
         {
            Iterator<?> elements = DOMUtils.getChildElements(schema, new QName("http://www.w3.org/2001/XMLSchema", "complexType"));
            while (elements.hasNext())
            {
               Element e = (Element)elements.next();
               boolean nameEquals = e.getAttribute("name").equals("UserException");
               if (nameEquals)
               {
                  secondTypeFound = true;
               }
            }
         }
      }
      assertTrue(firstTypeFound && secondTypeFound);
   }
}
