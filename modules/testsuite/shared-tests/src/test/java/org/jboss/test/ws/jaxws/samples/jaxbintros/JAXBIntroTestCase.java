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
package org.jboss.test.ws.jaxws.samples.jaxbintros;

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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test the JAXBIntroduction features.
 *
 * jaxb-intros.xml can reside under META-INF or WEB-INF and should be
 * picked up by JAXBIntroduction deployment aspect on server side.
 *
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class JAXBIntroTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-jaxbintros.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.jaxbintros.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.jaxbintros.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.samples.jaxbintros.UserType.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/jaxbintros/META-INF/jaxb-intros.xml"), "jaxb-intros.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-jaxbintros/EndpointService?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
      Iterator<Element> it = DOMUtils.getChildElements(wsdl, new QName("http://www.w3.org/2001/XMLSchema","attribute"), true);
      boolean attributeFound = false;
      while (it.hasNext())
      {
         Element el = it.next();
         if ("string".equals(el.getAttribute("name")))
         {
            attributeFound = true;
         }
      }
      assertTrue("<xs:attribute name=\"string\" ..> not found in wsdl", attributeFound);
   }

   /**
    * Client side uses the annotated user type class, server side uses the plain one but has jaxbintros in place
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testAnnotatedUserEndpoint() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-jaxbintros/EndpointService?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/samples/jaxbintros", "EndpointBeanService");

      Service service = Service.create(wsdlURL, serviceName);
      AnnotatedUserEndpoint port = service.getPort(AnnotatedUserEndpoint.class);
      AnnotatedUserType user = new AnnotatedUserType();
      QName qname = new QName("ns", "local", "prefix");
      user.setQname(qname);
      user.setString("Foo");
      AnnotatedUserType result = port.echo(user);
      assertEquals("Foo", result.getString());
      assertEquals(qname, result.getQname());
   }

}
