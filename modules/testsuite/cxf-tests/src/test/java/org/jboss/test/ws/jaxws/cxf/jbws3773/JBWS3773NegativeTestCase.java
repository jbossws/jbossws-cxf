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
package org.jboss.test.ws.jaxws.cxf.jbws3773;

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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies that decoupled WS-Addressing ReplyTo is rejected when
 * {@code org.jboss.ws.cxf.decoupledEndpointEnabled} is set to {@code false}.
 */
@ExtendWith(ArquillianExtension.class)
public class JBWS3773NegativeTestCase extends JBossWSTest
{
   private static final String CONTAINER_NAME = "jboss-decoupled-disabled";
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-jbws3773-negative";

   @ArquillianResource
   private ContainerController containerController;

   @ArquillianResource
   private Deployer deployer;

   @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
   @TargetsContainer(CONTAINER_NAME)
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
            .addManifest()
            .addClass(Greeter.class)
            .addClass(GreeterImpl.class)
            .addClass(TargetServlet.class);
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
   public void testDecoupledReplyToRejected() throws Exception
   {
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final URL baseURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final URL wsdlURL = new URL(baseURL + "SOAPService?wsdl");
      final QName qname = new QName("http://jboss.org/hello_world", "SOAPService");
      final Service service = Service.create(wsdlURL, qname);
      final Greeter greeter = service.getPort(Greeter.class, new AddressingFeature());

      AddressingProperties addrProperties = new AddressingProperties();
      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue(baseURL + "target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);

      BindingProvider provider = (BindingProvider) greeter;
      provider.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);

      SOAPFaultException ex = assertThrows(SOAPFaultException.class, () -> greeter.sayHi("Foo"));
      assertTrue(ex.getMessage().contains("is not permitted by this server"),
            "Expected 'not permitted' SOAPFault but got: " + ex.getMessage());
   }
}
