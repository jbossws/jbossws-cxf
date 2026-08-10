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
package org.jboss.test.ws.jaxws.cxf.jbws3516;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies that decoupled WS-Addressing FaultTo/ReplyTo is rejected when
 * {@code org.jboss.ws.cxf.decoupledEndpointEnabled} is set to {@code false}.
 */
@ExtendWith(ArquillianExtension.class)
public class JBWS3516NegativeTestCase extends JBossWSTest
{
   private static final String CONTAINER_NAME = "jboss-decoupled-disabled";
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-jbws3516-negative";

   @ArquillianResource
   private ContainerController containerController;

   @ArquillianResource
   private Deployer deployer;

   @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
   @TargetsContainer(CONTAINER_NAME)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf.impl\n"))
            .addPackages(false, new Filter<ArchivePath>() {
               @Override
               public boolean include(ArchivePath object)
               {
                  return !object.get().contains("TestCase");
               }}, "org.jboss.test.ws.jaxws.cxf.jbws3516")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3516/WEB-INF/wsdl/hello_world.wsdl"), "wsdl/hello_world.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3516/WEB-INF/web.xml"));
      return archive;
   }

   @BeforeEach
   public void startContainer()
   {
      if (!containerController.isStarted(CONTAINER_NAME))
      {
         containerController.start(CONTAINER_NAME);
      }
      deployer.deploy(DEPLOYMENT_NAME);
   }

   @AfterEach
   public void stopContainer()
   {
      try
      {
         deployer.undeploy(DEPLOYMENT_NAME);
      }
      catch (Exception e)
      {
         // ignore
      }
      if (containerController.isStarted(CONTAINER_NAME))
      {
         containerController.stop(CONTAINER_NAME);
      }
   }

   @Test
   @RunAsClient
   public void testDecoupledFaultToRejected() throws Exception
   {
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final String serverHost = getServerHost();
      final URL baseURL = new URL("http://" + serverHost + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final URL wsdlURL = new URL(baseURL + "helloworld?wsdl");
      final QName qname = new QName("http://jboss.org/hello_world", "SOAPService");
      final Service service = Service.create(wsdlURL, qname);
      final Greeter greeter = service.getPort(Greeter.class, new AddressingFeature());

      AddressingProperties addrProperties = new AddressingProperties();

      EndpointReferenceType faultTo = new EndpointReferenceType();
      AttributedURIType epr = new AttributedURIType();
      epr.setValue("http://" + serverHost + ":" + port + "/" + DEPLOYMENT_NAME + "/target/faultTo");
      faultTo.setAddress(epr);
      addrProperties.setFaultTo(faultTo);

      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue("http://" + serverHost + ":" + port + "/" + DEPLOYMENT_NAME + "/target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);

      BindingProvider provider = (BindingProvider) greeter;
      provider.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);

      SOAPFaultException ex = assertThrows(SOAPFaultException.class, () -> greeter.sayHi("hello"));
      assertTrue(ex.getMessage().contains("Unexpected EOF in prolog"),
            "Expected 'Unexpected EOF in prolog' SOAPFault but got: " + ex.getMessage());
   }
}
