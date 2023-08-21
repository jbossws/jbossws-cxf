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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.UseNewBusFeature;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A testcase for verifying proper behaviour of the UseNewBusFeature on
 * JAXWS Service creation.
 * 
 * A test case that verifies a client running inside and endpoint business method
 * does not use the deployment bus.
 * 
 * A test case that verifies Bus references do not leak into EJB3 clients 
 * 
 * A test case that verifies Bus references do not leak into servlet clients 
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Aug-2013
 *
 */
@RunWith(Arquillian.class)
public class BusTestCase extends JBossWSTest
{
   private static final String SERVER = "jaxws-cxf-bus";
   private static final String WSDL_SERVER = "jaxws-cxf-bus-wsdl";
   private static final String SERVLET_CLIENT_DEP = "jaxws-cxf-bus-servlet-client";
   private static final String EJB3_CLIENT_DEP = "jaxws-cxf-bus-ejb3-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @ArquillianResource
   Deployer deployer;
   
   @Deployment(name = SERVER, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVER + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.ClientEndpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.ClientEndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/permissions.xml"), "permissions.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF/web.xml"));
      return archive;
   }
   
   @Deployment(name = WSDL_SERVER, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WSDL_SERVER + ".war");
      archive.addManifest()
            .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/InvalidAddressEndpoint.wsdl"), "InvalidAddressEndpoint.wsdl")
            .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/ValidAddressEndpoint.wsdl"), "ValidAddressEndpoint.wsdl")
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.WSDLServlet.class);
      return archive;
   }
   
   @Deployment(name = SERVLET_CLIENT_DEP, testable = false, managed = false)
   public static WebArchive createDeployment3() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVLET_CLIENT_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services export,com.sun.xml.messaging.saaj services\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.AbstractClient.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.BusTestException.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointService.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.ServletClient.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF-client/permissions.xml"), "permissions.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF-client/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF-client/web.xml"));
      return archive;
   }
   
   @Deployment(name = EJB3_CLIENT_DEP, testable = false, managed = false)
   public static JavaArchive createDeployment4() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, EJB3_CLIENT_DEP + ".jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.AbstractClient.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.BusTestException.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.EJB3Client.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.EJB3ClientRemoteInterface.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointService.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/permissions.xml"), "permissions.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(WSDL_SERVER)
   public void testReuse() throws Exception
   {
      //odd wsdl GETs return WSDL doc with invalid soap:address
      //even wsdl GETs return WSDL doc with valid soap:address
      final String wsdl1 = readWsdl(baseURL); //invalid
      final String wsdl2 = readWsdl(baseURL); //valid
      final String wsdl3 = readWsdl(baseURL); //invalid
      final String wsdl4 = readWsdl(baseURL); //valid
      assertEquals(wsdl1, wsdl3);
      assertEquals(wsdl2, wsdl4);
      assertFalse(wsdl1.equals(wsdl2));
      
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);
         Endpoint port = getPort(baseURL, bus, new UseThreadBusFeature()); //invalid
         try {
            performInvocation(port);
            fail("Failure expected, as the wsdl soap:address is not valid!");
         } catch (WebServiceException wse) {
            assertTrue(wse.getCause().getMessage().contains("InvalidEndpoint"));
         }
         
         port = getPort(baseURL, bus, new UseNewBusFeature()); //valid
         //the port should now actually be built against the valid wsdl
         //as a new bus should have been started (with a new WSDLManager)
         //so the invocation is expected to succeed
         performInvocation(port);
      } finally {
         bus.shutdown(true);
      }
   }
   
   private String readWsdl(URL addr) throws Exception {
      return IOUtils.readAndCloseStream(addr.openStream());
   }
   
   protected static void performInvocation(Endpoint endpoint)
   {
      String result = endpoint.echo("Alessio");
      assert ("Alessio".equals(result));
   }
   
   protected static Endpoint getPort(URL wsdlAddr, Bus currentThreadBus, WebServiceFeature... features) throws MalformedURLException
   {
      QName serviceName = new QName("http://org.jboss.ws/bus", "EndpointService");
      Service service = Service.create(wsdlAddr, serviceName, features);
      //check the current thread bus has not changed (even if we used the UseNewBusFeature)
      assertEquals(currentThreadBus, BusFactory.getThreadDefaultBus(false));
      QName portQName = new QName("http://org.jboss.ws/bus", "EndpointPort");
      return (Endpoint) service.getPort(portQName, Endpoint.class);
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER)
   public void testBusLeakageInServletClient() throws Exception
   {
      deployer.deploy(SERVLET_CLIENT_DEP);
      try
      {
         final String host = baseURL.getHost() + ":" + baseURL.getPort();
         final String clientAddress = baseURL.getProtocol() + "://" + host + "/jaxws-cxf-bus-servlet-client";
         URL url = new URL(clientAddress + "?method=testBusCreation");
         assertEquals("OK testBusCreation", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(clientAddress + "?method=testSOAPConnection&host=" + host);
         assertEquals("OK testSOAPConnection", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(clientAddress + "?method=testWebServiceRef");
         assertEquals("OK testWebServiceRef", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(clientAddress + "?method=testWebServiceClient&host=" + host);
         assertEquals("OK testWebServiceClient", IOUtils.readAndCloseStream(url.openStream()));
      }
      finally
      {
         deployer.undeploy(SERVLET_CLIENT_DEP);
      }
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER)
   public void testBusLeakageInEJB3Client() throws Exception
   {
      deployer.deploy(EJB3_CLIENT_DEP);
      InitialContext iniCtx = null;
      try
      {
         String host = getServerHost() + ":" + getServerPort();
         iniCtx = getServerInitialContext();
         Object obj = iniCtx.lookup("jaxws-cxf-bus-ejb3-client//EJB3Client!" + EJB3ClientRemoteInterface.class.getName());
         EJB3ClientRemoteInterface ejb3Remote = (EJB3ClientRemoteInterface)obj;
         ejb3Remote.testBusCreation();
         ejb3Remote.testSOAPConnection(host);
         ejb3Remote.testWebServiceClient(host);
         ejb3Remote.testWebServiceRef();
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
         deployer.undeploy(EJB3_CLIENT_DEP);
      }
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER)
   public void testClient() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testClient("Foo", getServerHost() + ":" + getServerPort()));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER)
   public void testCachedPort() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testCachedPort("Foo", getServerHost() + ":" + getServerPort()));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER)
   public void testCachedService() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testCachedService("Foo", getServerHost() + ":" + getServerPort()));
   }
   
   private ClientEndpoint getPort() throws Exception {
      URL wsdlURL = new URL(baseURL + "ClientEndpointService/ClientEndpoint?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/bus", "ClientEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/bus", "ClientEndpointPort");
      return (ClientEndpoint) service.getPort(portQName, ClientEndpoint.class);
   }

}
