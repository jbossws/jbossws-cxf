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
package org.jboss.test.ws.jaxws.cxf.jaxbintros;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.IOUtils;
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
   private static final String DEP = "jaxws-cxf-jaxbintros";
   private static final String CLIENT_DEP = "jaxws-cxf-jaxbintros-client";
   
   private Helper helper;

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP, testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, DEP + ".jar");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.EndpointBean.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.UserType.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jaxbintros/META-INF/jaxb-intros.xml"), "jaxb-intros.xml");
      return archive;
   }

   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jaxbintros/META-INF/jaxb-intros.xml"), "jaxb-intros.xml")
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.AnnotatedUserEndpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.AnnotatedUserType.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.Helper.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jaxbintros.UserType.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jaxbintros/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jaxbintros/EndpointService?wsdl");
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
   
   private Helper getHelper() throws MalformedURLException
   {
      if (helper == null)
      {
         helper = new Helper(baseURL + "/jaxws-cxf-jaxbintros/EndpointService");
         helper.setJAXBIntroURL(getResourceURL("jaxws/cxf/jaxbintros/META-INF/jaxb-intros.xml"));
      }
      return helper;
   }

   /**
    * Both client and server side use plain UserType class but have jaxbintros in place to deal with customizations
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testEndpoint() throws Exception
   {
      assertTrue(getHelper().testEndpoint());
   }

   /**
    * Client side uses the annotated user type class, server side uses the plain one but has jaxbintros in place
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testAnnotatedUserEndpoint() throws Exception
   {
      assertTrue(getHelper().testAnnotatedUserEndpoint());
   }
   
   /**
    * Both client and server side use plain UserType class but have jaxbintros in place to deal with customizations
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testEndpointInContainer() throws Exception
   {
      assertEquals("1", runTestInContainer("testEndpoint"));
   }

   /**
    * Client side uses the annotated user type class, server side uses the plain one but has jaxbintros in place
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testAnnotatedUserEndpointInContainer() throws Exception
   {
      assertEquals("1", runTestInContainer("testAnnotatedUserEndpoint"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-jaxbintros/EndpointService&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
