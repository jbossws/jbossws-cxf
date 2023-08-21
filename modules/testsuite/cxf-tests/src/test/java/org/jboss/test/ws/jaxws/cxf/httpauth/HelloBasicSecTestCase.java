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
package org.jboss.test.ws.jaxws.cxf.httpauth;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ElytronClientTestUtils;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;

/**
 * @author ema@redhat.com
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class HelloBasicSecTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-basic-sec.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.ObjectFactory.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/basic/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/basic/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testBasicAuth() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "jbossws");
      int result = proxy.helloRequest("number");
      assertEquals(100, result);
      
   }

   @Test
   @RunAsClient
   public void testElytronConfigBasicAuth() throws Exception
   {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         ElytronClientTestUtils.setElytronClientConfig(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-http-basic-auth.xml");
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = (Hello) service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         int result = proxy.helloRequest("number");
         assertEquals(100, result);
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testElytronConfigDefaultAuth() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         ElytronClientTestUtils.setElytronClientConfig(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-default-auth.xml");
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = (Hello) service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         int result = proxy.helloRequest("number");
         assertEquals(100, result);
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testElytronConfigIncorrectPassword() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         AuthenticationConfiguration authenticationConfiguration =
                 AuthenticationConfiguration.empty()
                         .useName("jbossws")
                         .usePassword("incorrect");
         AuthenticationContext context = AuthenticationContext.empty();
         context = context.with(MatchRule.ALL, authenticationConfiguration);
         AuthenticationContext.getContextManager().setGlobalDefault(context);
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = (Hello) service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         try {
            proxy.helloRequest("number");
            fail("Authorization exception expected!");
         } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
         }
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testIgnoreElytronConfigWhenPropertiesSetBefore() throws Exception {
      AuthenticationContext previousAuthContext = AuthenticationContext.getContextManager().getGlobalDefault();
      try {
         AuthenticationConfiguration authenticationConfiguration =
                 AuthenticationConfiguration.empty()
                         .useName("jbossws")
                         .usePassword("password1!");
         AuthenticationContext context = AuthenticationContext.empty();
         context = context.with(MatchRule.ALL, authenticationConfiguration);
         AuthenticationContext.getContextManager().setGlobalDefault(context);
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Hello proxy = (Hello) service.getPort(Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "jbossws");
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
         cxfClientConfigurer.setConfigProperties(proxy, null, null);
         int result = proxy.helloRequest("number");
         assertEquals(100, result);
      } finally {
         AuthenticationContext.getContextManager().setGlobalDefault(previousAuthContext);
      }
   }

   @Test
   @RunAsClient
   public void testBasicAuthFail() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "wrongPwd");
      try {
         proxy.helloRequest("number");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }
   
   @Test
   @RunAsClient
   public void testBasicNoAuth() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
      try {
         proxy.helloRequest("number");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }
}
