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
package org.jboss.test.ws.jaxws.jbws3282;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the handlers (pre/post) declaration in jaxws endpoint configuration
 * 
 * https://issues.jboss.org/browse/JBWS-3282
 * https://issues.jboss.org/browse/JBWS-3836
 *
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2014
 */
@RunWith(Arquillian.class)
public class HandlerChainTestCaseForked extends JBossWSTest
{
   private final String targetNS = "http://jbws3282.jaxws.ws.test.jboss.org/";
   private static final String INCONTAINER_CLIENT = "jaxws-jbws3832-f-inContainer-client";
   private static final String DEP = "jaxws-jbws3282-f";

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = INCONTAINER_CLIENT, testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, INCONTAINER_CLIENT + ".war");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.jboss.as.server \n"))
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Helper.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.TestUtils.class)
            .addClass(org.jboss.test.helper.ClientHelper.class)
            .addClass(org.jboss.test.helper.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Deployment(name = DEP, testable = false, managed = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
         archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3282.EndpointHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint2Impl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint3Impl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3282.LogHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3282.RoutingHandler.class)
         .addAsResource("org/jboss/test/ws/jaxws/jbws3282/jaxws-handlers-server.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/WEB-INF/web-f.xml"));
      return archive;
   }

  @Test
  @RunAsClient
  public void testHandlerChainVanillaServer() throws Exception
   {
      try {
         deployer.deploy(DEP);
         QName serviceName = new QName(targetNS, "Endpoint2ImplService");
         URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3282-f/ep2?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         String resStr = port.echo("Kermit");
         assertEquals("Kermit|EpIn|endpoint2|EpOut", resStr);
         
         serviceName = new QName(targetNS, "Endpoint3ImplService");
         wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3282-f/ep3?wsdl");
         service = Service.create(wsdlURL, serviceName);
         port = (Endpoint)service.getPort(Endpoint.class);
         resStr = port.echo("Kermit");
         assertEquals("Kermit|EpIn|endpoint3|EpOut", resStr);
      } finally {
         deployer.undeploy(DEP);
      }
   }

   @Test
   @RunAsClient
   public void testHandlerChainModifiedServer() throws Exception
   {
      try {
         assertEquals("1", runTestInContainer("setupConfigurations"));
         try {
            deployer.deploy(DEP);
            QName serviceName = new QName(targetNS, "Endpoint2ImplService");
            URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3282-f/ep2?wsdl");
            Service service = Service.create(wsdlURL, serviceName);
            Endpoint port = (Endpoint)service.getPort(Endpoint.class);
            String resStr = port.echo("Kermit");
            assertEquals("Kermit|EpIn|RoutIn|endpoint2|RoutOut|EpOut", resStr);
            
            serviceName = new QName(targetNS, "Endpoint3ImplService");
            wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3282-f/ep3?wsdl");
            service = Service.create(wsdlURL, serviceName);
            port = (Endpoint)service.getPort(Endpoint.class);
            resStr = port.echo("Kermit");
            assertEquals("Kermit|LogIn|EpIn|endpoint3|EpOut|LogOut", resStr);
         } finally {
            deployer.undeploy(DEP);
         }
      } finally {
         assertEquals("1", runTestInContainer("restoreConfigurations"));
      }
   }

   // -------------------------
   
   private String runTestInContainer(String test) throws Exception
   {

      URL url = new URL("http://" + getServerHost()
         + ":" + getServerPort() + "/jaxws-jbws3832-f-inContainer-client?path=/jaxws-jbws3282-f&method=" + test
         + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
