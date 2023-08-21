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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy username ejb endpoint test case leveraging JAAS container integration and using digest passwords.
 * WS-SecurityPolicy 1.2 used for policies in the included wsdl contract.
 *
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com"/>Jim Ma<a>
 * @since 26-May-2011
 */
@RunWith(Arquillian.class)
public final class UsernameAuthorizationDigestEjbTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-wsse-policy-username-jaas-ejb-digest.jar");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.EJBDigestServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMe.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMeResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addAsManifestResource(
                  new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaas/ejb-digest/META-INF/jaxws-endpoint-config.xml"),
                  "jaxws-endpoint-config.xml")
            .addAsManifestResource(
                  new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaas/ejb-digest/META-INF/wsdl/SecurityService.wsdl"),
                  "wsdl/SecurityService.wsdl")
            .addAsManifestResource(
                  new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaas/ejb-digest/META-INF/wsdl/SecurityService_schema1.xsd"),
                  "wsdl/SecurityService_schema1.xsd");
      return archive;
   }
   
   @Test
   @RunAsClient
   public void test() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBDigestServiceImpl?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService"));
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   //JBWS-3843
   @Test
   @RunAsClient
   public void testConcurrent() throws Exception
   {
      ExecutorService executor = Executors.newFixedThreadPool(20);

      List<Callable<String>> taskList = new ArrayList<Callable<String>>();
      for (int i = 0; i < 20; i++)
      {
         taskList.add(new TestRunner());
      }
      List<Future<String>> resultList = executor.invokeAll(taskList);
      boolean passed = true;
      for (Future<String> future : resultList)
      {
         passed = passed && future.get().equals("Secure Hello World!");
      }
      assertTrue("Unexpected response from concurrent invocation", passed);

   }

   private class TestRunner implements Callable<String>
   {
      public String call() throws Exception
      {
         URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBDigestServiceImpl?wsdl");
         Service service = Service.create(wsdlURL, new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService"));
         ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
         setupWsse(proxy, "kermit");
         return proxy.sayHello();

      }

   }

   @Test
   @RunAsClient
   public void testUnauthenticated() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBDigestServiceImpl?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService"));
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      try
      {
         proxy.greetMe();
         fail("User kermit shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER,
            "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.UsernameDigestPasswordCallback");
   }
}
