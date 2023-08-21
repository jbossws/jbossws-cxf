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
package org.jboss.test.ws.jaxws.jbws1809;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test the JAXBIntroduction features.
 *
 * Check if the WSDL is generated correctly.
 * The introduction should turn a property into a xsd:attribute declaration.
 *
 * @author heiko.braun@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1809TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1809.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1809.DocRequest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1809.DocResponse.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1809.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1809.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1809/META-INF/jaxb-intros.xml"), "jaxb-intros.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1809/EndpointImpl?wsdl");

      Document doc = DOMUtils.getDocumentBuilder().parse(wsdlURL.toString());
      Element types = (Element)((Element)doc.getDocumentElement()
         .getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "types").item(0))
            .getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema").item(0);
      Iterator<?> it = DOMUtils.getChildElements(types, "complexType");

      boolean foundAttributeDeclaration = false;
      while(it.hasNext())
      {
         Element next = (Element)it.next();
         if(DOMUtils.getAttributeValue(next, "name").equals("docRequest"))
         {
            Iterator<?> it2 = DOMUtils.getChildElements(next, "attribute");

            while(it2.hasNext())
            {
               Element next2 = (Element)it2.next();
               if(DOMUtils.getAttributeValue(next2, "name").equals("value"))
               {
                  foundAttributeDeclaration = true;
               }
            }
         }
      }

      assertTrue("JAXBIntros should turn the 'docRequest.name' property into a XML attribute", foundAttributeDeclaration);
   }
}
