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
package org.jboss.test.ws.jaxws.samples.wsa;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies that client-side decoupled endpoint is rejected by the server when
 * {@code org.jboss.ws.cxf.decoupledEndpointEnabled} is set to {@code false}.
 */
@ExtendWith(ArquillianExtension.class)
public final class AddressingNegativeTestCase extends JBossWSTest
{
   private static final String CONTAINER_NAME = "jboss-decoupled-disabled";
   private static final String DEPLOYMENT_NAME = "jaxws-samples-wsa-negative";

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
            .addClass(ServiceIface.class)
            .addClass(ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.jaxws.SayHelloResponse.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsa/WEB-INF/web.xml"));
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
   public void testDecoupledEndpointRejected() throws Exception
   {
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try
      {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
         URL wsdlURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/AddressingService?wsdl");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

         Client client = ClientProxy.getClient(proxy);
         HTTPConduit conduit = (HTTPConduit) client.getConduit();
         HTTPClientPolicy policy = conduit.getClient();
         policy.setConnectionTimeout(5000);
         policy.setReceiveTimeout(10000);
         policy.setDecoupledEndpoint("http://" + getServerHost() + ":18181/jaxws-samples-wsa-negative/decoupled-endpoint");

         SOAPFaultException ex = assertThrows(SOAPFaultException.class, () -> proxy.sayHello("Sleepy"));
         assertTrue(ex.getMessage().contains("is not permitted by this server"),
               "Expected 'not permitted' SOAPFault but got: " + ex.getMessage());
      }
      finally
      {
         bus.shutdown(true);
      }
   }
}
