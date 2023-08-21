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
import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback;
import org.jboss.wsf.test.CryptoCheckHelper;
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
 * @since 07-Sep-2012
 */
@RunWith(Arquillian.class)
public final class WSSecurityPolicyExamples22xTestCase extends JBossWSTest
{
   private final String NS = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples";
   private final QName serviceName = new QName(NS, "SecurityService");

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-oasis-22x.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service221Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service222Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service223Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service224Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-oasis-22x-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.jks"), "alice.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.properties"), "alice.properties");
         }
      });
   }
   
   /**
    * 2.2.1 (WSS1.0) X.509 Certificates, Sign, Encrypt
    * 
    * This use-case corresponds to the situation where both parties have X.509v3 certificates (and public-private key pairs).
    * The requestor identifies itself to the service. The message exchange is integrity protected and encrypted.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test221() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "SecurityService221?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService221Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.2  (WSS1.0) Mutual Authentication with X.509 Certificates, Sign, Encrypt
    * 
    * This use case corresponds to the situation where both parties have X.509v3 certificates (and public-private key pairs).
    * The requestor wishes to identify itself to the service using its X.509 credential (strong authentication).
    * The message exchange needs to be integrity protected and encrypted as well. The difference from previous use case is
    * that the X509 token inserted by the client is included in the message signature (see <ProtectTokens />).
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test222() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "SecurityService222?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService222Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) Mutual Authentication with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.3  (WSS1.1) Anonymous with X.509 Certificate, Sign, Encrypt
    * 
    * In this use case the Request is signed using DerivedKeyToken1(K), then encrypted using a DerivedKeyToken2(K) where K is ephemeral key
    * protected for the server's certificate. Response is signed using DKT3(K), (if needed) encrypted using DKT4(K). The requestor does no
    * wish to identify himself; the message exchange is protected using derived symmetric keys. As a simpler, but less secure, alternative,
    * ephemeral key K (instead of derived keys) could be used for message protection by simply omitting the sp:RequireDerivedKeys assertion.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test223() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "SecurityService223?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService223Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) Anonymous with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.4  (WSS1.1) Mutual Authentication with X.509 Certificates, Sign, Encrypt
    * 
    * Client and server X509 certificates are used for client and server authorization respectively. Request is signed using K, then
    * encrypted using K, K is ephemeral key protected for server's certificate. Signature corresponding to K is signed using client certificate.
    * Response is signed using K, encrypted using K, encrypted key K is not included in response. Alternatively, derived keys can be used for
    * each of the encryption and signature operations by simply adding an sp:RequireDerivedKeys assertion.
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test224() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "SecurityService224?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService224Port"), ServiceIface.class);
      setupWsse(proxy, false);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) Mutual Authentication with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   private void setupWsse(ServiceIface proxy, boolean streaming)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
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
