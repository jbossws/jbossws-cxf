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
package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy examples
 *
 * From OASIS WS-SecurityPolicy Examples Version 1.0
 * http://docs.oasis-open.org/ws-sx/security-policy/examples/ws-sp-usecases-examples.html
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Sep-2012
 */
@RunWith(Arquillian.class)
public final class WSSecurityPolicyExamples21xTestCase extends JBossWSTest
{
   private final String NS = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples";
   private final QName serviceName = new QName(NS, "SecurityService");

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-oasis-21x.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServerUsernamePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2111Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2112Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2113Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2121Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service213Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service214Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService21x.wsdl"), "wsdl/SecurityService21x.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-oasis-21x-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.jks"), "alice.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.properties"), "alice.properties");
         }
      });
   }

   /**
    * 2.1.1.1 UsernameToken with plain text password
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2111() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/SecurityService2111?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2111Port"), ServiceIface.class);
      setupWsse(proxy, true);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken with plain text password"));
   }

   /**
    * 2.1.1.2 UsernameToken without password
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2112() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/SecurityService2112?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2112Port"), ServiceIface.class);
      setupWsse(proxy, false);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken without password"));
   }

   /**
    * 2.1.1.3  UsernameToken with timestamp, nonce and password hash
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2113() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/SecurityService2113?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2113Port"), ServiceIface.class);
      setupWsse(proxy, true);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken with timestamp, nonce and password hash"));
   }

   /**
    * 2.1.2.1  UsernameToken as supporting token
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2121() throws Exception
   {
      Service service = Service.create(new URL("https", baseURL.getHost(), (baseURL.getPort() - 8080 + 8443), "/jaxws-samples-wsse-policy-oasis-21x/SecurityService2121?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2121Port"), ServiceIface.class);
      setupWsse(proxy, false);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken as supporting token"));
   }

   /**
    * 2.1.3  (WSS 1.0) UsernameToken with Mutual X.509v3 Authentication, Sign, Encrypt
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test213() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/SecurityService213?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService213Port"), ServiceIface.class);
      setupWsse(proxy, true);
      assertTrue(proxy.sayHello().equals("Hello - (WSS 1.0) UsernameToken with Mutual X.509v3 Authentication, Sign, Encrypt"));
   }

   /**
    * 2.1.4  (WSS 1.1) User Name with Certificates, Sign, Encrypt
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test214() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/SecurityService214?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService214Port"), ServiceIface.class);
      setupWsse(proxy, false);
      assertTrue(proxy.sayHello().equals("Hello - (WSS 1.1) User Name with Certificates, Sign, Encrypt"));
   }
   
   private void setupWsse(ServiceIface proxy, boolean streaming)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, "kermit");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new UsernamePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      if (streaming)
      {
         ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
         ((BindingProvider)proxy).getResponseContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
      }
   }
}
