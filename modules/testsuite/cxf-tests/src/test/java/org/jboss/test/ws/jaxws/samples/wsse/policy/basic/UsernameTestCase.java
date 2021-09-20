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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

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
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;

/**
 * WS-Security Policy username test case
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class UsernameTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-username-unsecure-transport.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.JavaFirstServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.JavaFirstServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServerUsernamePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username-unsecure-transport/JavaFirstPolicy.xml"), "classes/JavaFirstPolicy.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username-unsecure-transport/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username-unsecure-transport/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username-unsecure-transport/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username-unsecure-transport/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(baseURL + "/service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse((BindingProvider)proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   public void testUsernameTokenElytronClientConfig() throws Exception
   {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         ElytronClientTestUtils.setElytronClientConfig(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-username-token.xml");
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         URL wsdlURL = new URL(baseURL + "/service?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = service.getPort(ServiceIface.class);
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         assertEquals("Secure Hello World!", proxy.sayHello());
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testUsernameTokenElytronClientConfigIncorrectPassword() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         AuthenticationConfiguration authenticationConfiguration =
                 AuthenticationConfiguration.empty()
                         .useName("kermit")
                         .usePassword("incorrect");

         AuthenticationContext context = AuthenticationContext.empty();
         context = context.with(MatchRule.ALL, authenticationConfiguration);
         AuthenticationContext.getContextManager().setGlobalDefault(context);
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         URL wsdlURL = new URL(baseURL + "/service?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = service.getPort(ServiceIface.class);
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         try {
            proxy.sayHello();
            fail("User kermit shouldn't be authenticated.");
         } catch (Exception e) {
            //OK
         }
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testUsernameTokenIgnoreElytronClientConfigWhenPropertiesSetBefore() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         AuthenticationConfiguration authenticationConfiguration =
                 AuthenticationConfiguration.empty()
                         .useName("kermit")
                         .usePassword("incorrect");
         AuthenticationContext context = AuthenticationContext.empty();
         context = context.with(MatchRule.ALL, authenticationConfiguration);
         AuthenticationContext.getContextManager().setGlobalDefault(context);
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         URL wsdlURL = new URL(baseURL + "/service?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = service.getPort(ServiceIface.class);
         setupWsse((BindingProvider) proxy, "kermit");
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         assertEquals("Secure Hello World!", proxy.sayHello());
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testWrongPassword() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(baseURL + "/service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse((BindingProvider)proxy, "snoopy");
      try
      {
         proxy.sayHello();
         fail("User snoopy shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   @Test
   @RunAsClient
   public void testNoCBH() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(baseURL + "/service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsseNoCBH((BindingProvider)proxy, "kermit", "thefrog");
      assertEquals("Secure Hello World!", proxy.sayHello());
      setupWsseNoCBH((BindingProvider)proxy, "kermit", "wrongpassword");
      try
      {
         proxy.sayHello();
         fail("User snoopy shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   @Test
   @RunAsClient
   public void testJavaFirst() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "JavaFirstSecurityService");
      URL wsdlURL = new URL(baseURL + "/javafirst-service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      JavaFirstServiceIface proxy = (JavaFirstServiceIface)service.getPort(JavaFirstServiceIface.class);
      setupWsse((BindingProvider)proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   public void testJavaFirstWrongPassword() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "JavaFirstSecurityService");
      URL wsdlURL = new URL(baseURL + "/javafirst-service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      JavaFirstServiceIface proxy = (JavaFirstServiceIface)service.getPort(JavaFirstServiceIface.class);
      setupWsse((BindingProvider)proxy, "snoopy");
      try
      {
         proxy.sayHello();
         fail("User snoopy shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   private void setupWsse(BindingProvider proxy, String username)
   {
      proxy.getRequestContext().put(SecurityConstants.USERNAME, username);
      proxy.getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.UsernamePasswordCallback");
   }
   
   private void setupWsseNoCBH(BindingProvider proxy, String username, String password)
   {
      proxy.getRequestContext().put(SecurityConstants.USERNAME, username);
      proxy.getRequestContext().put(SecurityConstants.PASSWORD, password);
   }
}
