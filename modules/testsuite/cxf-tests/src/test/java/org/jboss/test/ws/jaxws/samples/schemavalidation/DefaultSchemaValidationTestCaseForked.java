/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.schemavalidation;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.schemavalidation.types.HelloResponse;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A testcase for verifying default schema validation configured
 * through standard client/endpoint configuration (AS 7 DMR)
 * 
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class DefaultSchemaValidationTestCaseForked extends JBossWSTest
{
   private static final String DEPLOYMENT = "jaxws-samples-schemavalidation";
   
   @ArquillianResource
   private URL baseURL;
   
   @ArquillianResource
   Deployer deployer;
   
   @Deployment(testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-schemavalidation-client.war");
      archive
             .setManifest(new StringAsset("Manifest-Version: 1.0\n"
              + "Dependencies: org.jboss.as.server\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Helper.class)
            .addPackage("org.jboss.test.ws.jaxws.samples.schemavalidation.types")
            .addClass(org.jboss.wsf.test.ClientHelper.class)
            .addClass(org.jboss.wsf.test.TestServlet.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/client.wsdl"), "classes/client.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/validatingClient.wsdl"), "classes/validatingClient.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }
   
   @Deployment(name = DEPLOYMENT, testable = false, managed = false)
   public static WebArchive createServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT + ".war");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.HelloImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.ValidatingHelloImpl.class)
            .addPackage("org.jboss.test.ws.jaxws.samples.schemavalidation.types")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/web.xml"));
      return archive;
   }
   
   /**
    * Verifies the default client configuration can be used to always set schema validation from AS model
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testDefaultClientValidation() throws Exception {
      try {
         deployer.deploy(DEPLOYMENT);
         assertEquals("1", runInContainer("testDefaultClientValidation"));
      } finally {
         deployer.undeploy(DEPLOYMENT);
      }
   }
   
   /**
    * Verifies the default endpoint configuration can be used to always set schema validation from AS model
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testDefaultServerValidation() throws Exception {
      final QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      final QName portName = new QName("http://jboss.org/schemavalidation", "HelloPort");
      URL wsdlURL = getResourceURL("jaxws/samples/schemavalidation/client.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello) service.getPort(portName, Hello.class);
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-samples-schemavalidation/hello");
      HelloResponse hr;
      try {
         deployer.deploy(DEPLOYMENT);
         hr = proxy.helloRequest("JBoss");
         assertNotNull(hr);
         assertEquals(2, hr.getReturn());
         hr = proxy.helloRequest("number");
         assertNotNull(hr);
         assertEquals(2, hr.getReturn());
      } finally {
         deployer.undeploy(DEPLOYMENT);
      }
      
      // -- modify default conf to enable default endpoint schema validation
      try
      {
         runInContainer("enableDefaultEndpointSchemaValidation");
         try {
            deployer.deploy(DEPLOYMENT);
            hr = proxy.helloRequest("JBoss");
            assertNotNull(hr);
            assertEquals(2, hr.getReturn());
            try {
               proxy.helloRequest("number");
               fail();
            } catch (Exception e) {
               assertTrue(e.getMessage().contains("is not facet-valid with respect to enumeration"));
            }
         } finally {
            deployer.undeploy(DEPLOYMENT);
         }
      }
      finally
      {
         // -- restore default conf --
         runInContainer("disableDefaultEndpointSchemaValidation");
         // --
      }
      
      
   }
   
   // -------------------------
   
   private String runInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-samples-schemavalidation/hello&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }

   
}