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
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.AddressingFeature;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Client invoking web service using WS-Addressing
 *
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public final class AddressingTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsa.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsa.jaxws.SayHelloResponse.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsa/WEB-INF/web.xml"));
      return archive;
   }

   /**
    * This tests the invocation using the local copy of the service contract; that does not have any ws-addressing
    * policy, so the addressing feature needs to be explicitly provided.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testUsingLocalContract() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = getResourceURL("jaxws/samples/wsa/WEB-INF/wsdl/AddressingService.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class, new AddressingFeature());
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/jaxws-samples-wsa/AddressingService");
      // invoke method
      assertEquals("Hello World!", proxy.sayHello("World"));
   }
   
   /**
    * This tests the invocation using the service contract published by the endpoint. That should have the
    * ws-addressing policy in it, hence no need to explicitly configure addressing, the policy engine takes care of that.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testUsingContractFromDeployedEndpoint() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsa/AddressingService?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      // invoke method
      assertEquals("Hello World!", proxy.sayHello("World"));
   }
   
   /**
    * This shows the usage of decoupled-endpoint for getting back response on a new http connection.
    * The CXF client basically creates a destination listening at the provided decoupled endpoint address, using the
    * configured http transport factory. The client gets back a HTTP 202 accept response message immediately after
    * the call to the server, then once the actual response comes back to the decoupled endpoint, the client is
    * notified and returns control to the application code.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testDecoupledEndpointForLongLastingProcessingOfInvocations() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         // construct proxy
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
         URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsa/AddressingService?wsdl");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
         
         Client client = ClientProxy.getClient(proxy);
         HTTPConduit conduit = (HTTPConduit)client.getConduit();
         HTTPClientPolicy policy = conduit.getClient();
         //set low connection and receive timeouts to ensure the http client can't keep the connection open till the response is received
         policy.setConnectionTimeout(5000); //5 secs
         policy.setReceiveTimeout(10000); //10 secs
         //please note you might want to set the synchronous timeout for long waits, as CXF ClientImpl would simply drop waiting for the response after that (default 60 secs)
//          ((ClientImpl)client).setSynchronousTimeout(value);
         
         try {
            proxy.sayHello("Sleepy"); //this takes at least 30 secs
            fail("Timeout exception expected");
         } catch (WebServiceException e) {
            assertTrue(e.getCause() instanceof SocketTimeoutException);
         }
         
         policy.setDecoupledEndpoint("http://" + getServerHost() + ":18181/jaxws-samples-wsa/decoupled-endpoint");
         String response = proxy.sayHello("Sleepy"); //this takes at least 30 secs... but now the client doesn't time out
         assertEquals("Hello Sleepy!", response);
      } finally {
         bus.shutdown(true);
      }
   }
   
   
}
