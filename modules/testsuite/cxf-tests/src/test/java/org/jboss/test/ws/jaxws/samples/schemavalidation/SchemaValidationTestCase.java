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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.schemavalidation.types.HelloResponse;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A testcase acting as sample for using client and server schema validation of messages
 * 
 * @author ema@redhat.com
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class SchemaValidationTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-schemavalidation.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.apache.cxf\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.HelloImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.ValidatingHelloImpl.class)
         .addPackage("org.jboss.test.ws.jaxws.samples.schemavalidation.types")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/web.xml"));
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-schemavalidation-client.jar") { {
               archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }
   
   @Test
   @RunAsClient
   public void testSchemaValidationEndpoint() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = JBossWSTestHelper.getResourceURL("jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "ValidatingHelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/validatingHello");
      HelloResponse hr = proxy.helloRequest("JBoss"); //valid value (see xsd restriction in the wsdl)
      assertNotNull(hr);
      assertEquals(1, hr.getReturn());
      try {
         proxy.helloRequest("number");
         fail("validation error is expected");
      } catch (Exception e) {
         assertTrue("not respect to enumration error is expected", e.getMessage().contains("is not facet-valid with respect to enumeration"));
      }
   }
   
   @Test
   @RunAsClient
   public void testNoSchemaValidationEndpoint() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = JBossWSTestHelper.getResourceURL("jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "HelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/hello");
      HelloResponse hr = proxy.helloRequest("JBoss");
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
      hr = proxy.helloRequest("number"); //validation is not enabled...
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
   }
   
   @Test
   @RunAsClient
   public void testClientSideSchemaValidation() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = JBossWSTestHelper.getResourceURL("jaxws/samples/schemavalidation/validatingClient.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "HelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/hello");
      ((BindingProvider)proxy).getRequestContext().put("schema-validation-enabled", true); //enable client side schema validation
      HelloResponse hr = proxy.helloRequest("JBoss");
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
      try {
         proxy.helloRequest("number");
         fail("validation error is expected");
      } catch (Exception e) {
         assertTrue("not respect to enumration error is expected", e.getMessage().contains("is not facet-valid with respect to enumeration"));
      }
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testClientSideSchemaValidationUsingConfiguration() throws Exception
   {
      try {
         
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = JBossWSTestHelper.getResourceURL("jaxws/samples/schemavalidation/validatingClient.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "HelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/hello");
      ClientConfigUtil.setConfigProperties(proxy, "META-INF/jaxws-client-config.xml", "Test Validating Client Config"); //enable client side schema validation
      HelloResponse hr = proxy.helloRequest("JBoss");
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
      try {
         proxy.helloRequest("number");
         fail("validation error is expected");
      } catch (Exception e) {
         assertTrue("not respect to enumration error is expected", e.getMessage().contains("is not facet-valid with respect to enumeration"));
      }
      } finally {
         
      }
   }
}