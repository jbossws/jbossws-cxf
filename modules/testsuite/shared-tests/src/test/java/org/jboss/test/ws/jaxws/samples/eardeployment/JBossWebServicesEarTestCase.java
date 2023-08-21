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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3865] Test jboss-webservices.xml EAR override
 * 
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class JBossWebServicesEarTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static EnterpriseArchive createDeployment() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-jbosswebservices-eardeployment-ejb3.jar");
      archive1
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.logging\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.EJB3Bean.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/TestService.xsd"), "wsdl/TestService.xsd");

      WebArchive archive2 = ShrinkWrap.create(WebArchive.class, "jaxws-samples-jbosswebservices-eardeployment-pojo.war");
      archive2
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.logging,org.jboss.ws.common,org.jboss.as.server\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.JSEBean.class)
         .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.SupportServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/jboss-web2.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/TestService.xsd"), "wsdl/TestService.xsd")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/web.xml"));

      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-samples-jbosswebservices-eardeployment.ear");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.as.server\n"))
         .addAsModule(archive1)
         .addAsModule(archive2)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/META-INF/jboss-webservices.xml"), "jboss-webservices.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEJB3Endpoint() throws Exception
   {
      String soapAddress = "http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/earejb3/EndpointService/Endpoint";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      Service service = Service.create(new URL(soapAddress + "?wsdl"), serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      //check soap address rewrite to verify jboss-webservices.xml override
      Definition wsdl = getWSDLDefinition(soapAddress + "?wsdl");
      SOAPAddress sa = (SOAPAddress)wsdl.getService(serviceName).getPort("EndpointPort").getExtensibilityElements().iterator().next();
      assertEquals("https://foo-jar:" + JBossWSTestHelper.getSecureServerPort(null, null) + "/earejb3/EndpointService/Endpoint", sa.getLocationURI());

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   public void testJSEEndpoint() throws Exception
   {
      String soapAddress = baseURL + "JSEBean";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      Service service = Service.create(new URL(soapAddress + "?wsdl"), serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      //check soap address rewrite to verify jboss-webservices.xml override
      Definition wsdl = getWSDLDefinition(soapAddress + "?wsdl");
      SOAPAddress sa = (SOAPAddress)wsdl.getService(serviceName).getPort("EndpointPort").getExtensibilityElements().iterator().next();
      assertEquals("https://foo-test:" + JBossWSTestHelper.getSecureServerPort(null, null) + "/earjse2/JSEBean", sa.getLocationURI());

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }
}
