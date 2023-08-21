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
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.CryptoCheckHelper;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Before;
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
public final class WSSecurityPolicyExamples23xTestCase extends JBossWSTest
{
   private static final String DEPLOYMENT = "jaxws-samples-wsse-policy-oasis-23x";
   private static final String SSL_MUTUAL_AUTH_SERVER = "ssl-mutual-auth"; 
   
   private final String NS = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples";
   private final String serviceURL = "http://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, SSL_MUTUAL_AUTH_SERVER) + "/jaxws-samples-wsse-policy-oasis-23x/";
   private final String serviceURLHttps = "https://" + getServerHost() + ":" + (getServerPort(CXF_TESTS_GROUP_QUALIFIER, SSL_MUTUAL_AUTH_SERVER) + 363) + "/jaxws-samples-wsse-policy-oasis-23x/";
   private final QName serviceName = new QName(NS, "SecurityService");

   @ArquillianResource
   private Deployer deployer;
   
   @ArquillianResource
   private ContainerController containerController;
   
   @Deployment(name = DEPLOYMENT, testable = false, managed = false)
   @TargetsContainer(SSL_MUTUAL_AUTH_SERVER)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2311Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2312Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2313Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2314Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2315Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2321Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2322Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2323Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.Service2324Impl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.SAMLValidator.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/bob.properties"), "classes/bob.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService23x.wsdl"), "wsdl/SecurityService23x.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      return archive;
   }
   
   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-oasis-23x-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.jks"), "alice.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/oasis/META-INF/alice.properties"), "alice.properties");
         }
      });
   }
   
   @Before
   public void startContainerAndDeploy() throws Exception {
      if (!containerController.isStarted(SSL_MUTUAL_AUTH_SERVER)) {
         containerController.start(SSL_MUTUAL_AUTH_SERVER);
         deployer.deploy(DEPLOYMENT);
      }
   }
   
   /**
    * 2.3.1.1 (WSS1.0) SAML1.1 Assertion (Bearer)
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(DEPLOYMENT)
   public void test2311() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2311?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2311Port"), ServiceIface.class);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SAML_CALLBACK_HANDLER, new SamlCallbackHandler());
      assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) SAML1.1 Assertion (Bearer)"));
   }

   /**
    * 2.3.1.2 (WSS1.0) SAML1.1 Assertion (Sender Vouches) over SSL
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(DEPLOYMENT)
   public void test2312() throws Exception
   {
      Service service = Service.create(new URL(serviceURLHttps + "SecurityService2312?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2312Port"), ServiceIface.class);
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) SAML1.1 Assertion (Sender Vouches) over SSL"));
   }
   
   /**
    * 2.3.1.3 (WSS1.0) SAML1.1 Assertion (HK) over SSL
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2313() throws Exception
   {
      Service service = Service.create(new URL(serviceURLHttps + "SecurityService2313?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2313Port"), ServiceIface.class);
      Map<String, Object> reqCtx = ((BindingProvider) proxy).getRequestContext();
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:holder-of-key");
      cbh.setSigned(true);
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      assertTrue(proxy.sayHello().equals("Hello -  (WSS1.0) SAML1.1 Assertion (HK) over SSL"));
   }

   /**
    * 2.3.1.4 (WSS1.0) SAML1.1 Sender Vouches with X.509 Certificates, Sign, Optional Encrypt
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2314() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2314?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2314Port"), ServiceIface.class);
      Map<String, Object> reqCtx = ((BindingProvider) proxy).getRequestContext();
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches");
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) SAML1.1 Sender Vouches with X.509 Certificates, Sign, Optional Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.3.1.5 (WSS1.0) SAML1.1 Holder of Key, Sign, Optional Encrypt
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2315() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2315?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2315Port"), ServiceIface.class);
      Map<String, Object> reqCtx = ((BindingProvider) proxy).getRequestContext();
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:holder-of-key");
      cbh.setSigned(true);
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) SAML1.1 Holder of Key, Sign, Optional Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.3.2.1 (WSS1.1) SAML 2.0 Bearer
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2321() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2321?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2321Port"), ServiceIface.class);
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
      cbh.setSaml2(true);
      Map<String, Object> reqCtx = ((BindingProvider)proxy).getRequestContext();
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) SAML 2.0 Bearer"));
   }

   /**
    * 2.3.2.2 (WSS1.1) SAML2.0 Sender Vouches over SSL
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2322() throws Exception
   {
      Service service = Service.create(new URL(serviceURLHttps + "SecurityService2322?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2322Port"), ServiceIface.class);
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:2.0:cm:sender-vouches");
      cbh.setSaml2(true);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) SAML2.0 Sender Vouches over SSL"));
   }
   
   /**
    * 2.3.2.3 (WSS1.1) SAML2.0 HoK over SSL
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2323() throws Exception
   {
      Service service = Service.create(new URL(serviceURLHttps + "SecurityService2323?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2323Port"), ServiceIface.class);
      Map<String, Object> reqCtx = ((BindingProvider) proxy).getRequestContext();
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:2.0:cm:holder-of-key");
      cbh.setSaml2(true);
      cbh.setSigned(true);
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) SAML2.0 HoK over SSL"));
   }

   /**
    * 2.3.2.4 (WSS1.1) SAML1.1/2.0 Sender Vouches with X.509 Certificate, Sign, Encrypt
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test2324() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2324?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2324Port"), ServiceIface.class);
      Map<String, Object> reqCtx = ((BindingProvider) proxy).getRequestContext();
      SamlCallbackHandler cbh = new SamlCallbackHandler();
      cbh.setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches");
      reqCtx.put(SecurityConstants.SAML_CALLBACK_HANDLER, cbh);
      reqCtx.put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      reqCtx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      reqCtx.put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      reqCtx.put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) SAML1.1/2.0 Sender Vouches with X.509 Certificate, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }
}
