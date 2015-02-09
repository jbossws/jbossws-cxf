/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.CryptoHelper;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-SecurityPolicy code first dev test
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Jun-2013
 */
@RunWith(Arquillian.class)
public final class AnnotatedSignEncryptTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-sign-encrypt-gcm-code-first.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.AnnotatedServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.AnnotatedServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/gcm/WEB-INF/bob.properties"), "classes/bob.properties");
      return archive;
   }
   
   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("AnnotatedSignEncryptTestCase-client.jar") { {
      archive
         .addManifest()
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.jks"), "alice.jks")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.properties"), "alice.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }

   @Test
   @RunAsClient
   public void testWsdl() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/AnnotatedSecurityService?wsdl");
      assertTrue(IOUtils.readAndCloseStream(wsdlURL.openStream()).contains("AsymmetricBinding_X509v1_GCM256OAEP_ProtectTokens_binding_policy"));
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test() throws Exception
   {
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "AnnotatedSecurityService");
         URL wsdlURL = new URL(baseURL + "/AnnotatedSecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         AnnotatedServiceIface proxy = (AnnotatedServiceIface)service.getPort(AnnotatedServiceIface.class);
         setupWsse(proxy);
         ((BindingProvider)proxy).getRequestContext().put(Message.RECEIVE_TIMEOUT, 120000);
         assertEquals("Secure Hello World!", proxy.sayHello());
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   private void setupWsse(AnnotatedServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
   }
}
