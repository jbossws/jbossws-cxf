/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.samples.wsa;

import java.net.SocketTimeoutException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.Test;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Client invoking web service using WS-Addressing
 *
 * @author richard.opalka@jboss.com
 */
public final class AddressingTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsa/AddressingService";
   
   public static Test suite()
   {
      return new JBossWSTestSetup(AddressingTestCase.class, "jaxws-samples-wsa.war");
   }

   /**
    * This tests the invocation using the local copy of the service contract; that does not have any ws-addressing
    * policy, so the addressing feature needs to be explicitly provided.
    * 
    * @throws Exception
    */
   public void testUsingLocalContract() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = getResourceURL("jaxws/samples/wsa/WEB-INF/wsdl/AddressingService.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class, new AddressingFeature());
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      // invoke method
      assertEquals("Hello World!", proxy.sayHello("World"));
   }
   
   /**
    * This tests the invocation using the service contract published by the endpoint. That should have the
    * ws-addressing policy in it, hence no need to explicitly configure addressing, the policy engine takes care of that.
    * 
    * @throws Exception
    */
   public void testUsingContractFromDeployedEndpoint() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
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
   public void testDecoupledEndpointForLongLastingProcessingOfInvocations() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      
      Client client = ClientProxy.getClient(proxy);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy policy = conduit.getClient();
      //set low connection and receive timeouts to ensure the http client can't keep the connection open till the response is received
      policy.setConnectionTimeout(5000); //5 secs
      policy.setReceiveTimeout(10000); //10 secs
      //please note you might want to set the synchronous timeout for long waits, as CXF ClientImpl would simply drop waiting for the response after that (default 60 secs)
//      ((ClientImpl)client).setSynchronousTimeout(value);
      
      try {
         proxy.sayHello("Sleepy"); //this takes at least 30 secs
         fail("Timeout exception expected");
      } catch (WebServiceException e) {
         assertTrue(e.getCause() instanceof SocketTimeoutException);
      }
      
      policy.setDecoupledEndpoint("http://localhost:18181/jaxws-samples-wsa/decoupled-endpoint");
      String response = proxy.sayHello("Sleepy"); //this takes at least 30 secs... but now the client doesn't time out
      assertEquals("Hello Sleepy!", response);
   }
   
   
}
