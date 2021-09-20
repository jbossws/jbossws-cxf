/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.secconv;

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
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Secure Conversation testcase
 *
 * From OASIS WS-SecurityPolicy Examples Version 1.0:
 * 
 * 2.4.1 (WSS 1.0) Secure Conversation bootstrapped by Mutual
 * Authentication with X.509 Certificates
 *
 * @author alessio.soldano@jboss.com
 * @since 06-Sep-2012
 */
@RunWith(Arquillian.class)
public final class SecureConversationTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-secconv.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.secconv.KeystorePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.secconv.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.secconv.ServiceImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-secconv-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/secconv/META-INF/alice.properties"), "alice.properties");
         }
      });
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-wsse-policy-secconv/SecureConversationService?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, true);
      assertTrue(proxy.sayHello().startsWith("Secure Conversation Hello World!"));
      assertTrue(proxy.sayHello().startsWith("Secure Conversation Hello World!"));
   }

   private void setupWsse(ServiceIface proxy, boolean streaming)
   {
      ((BindingProvider)proxy).getRequestContext().put("ws-security.callback-handler.sct", new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put("ws-security.signature.properties.sct", Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put("ws-security.encryption.properties.sct", Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put("ws-security.signature.username.sct", "alice");
      ((BindingProvider)proxy).getRequestContext().put("ws-security.encryption.username.sct", "bob");
      if (streaming)
      {
         ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
         ((BindingProvider)proxy).getResponseContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
      }
   }
}
