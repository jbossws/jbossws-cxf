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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * WS-Security Policy username ejb endpoint test case leveraging JAAS container integration and using digest passwords.
 * WS-SecurityPolicy 1.2 used for policies in the included wsdl contract.
 *
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com"/>Jim Ma<a>
 * @since 26-May-2011
 */
public final class UsernameAuthorizationDigestEjbTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-jaas-ejb-digest/SecurityService/EJBServiceImpl";
   private QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");

   public static BaseDeployment<?>[] createDeployments()
   {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-username-jaas-ejb-digest.jar") {
         {  //add org.jboss.as.webservices.server.integration dependency to load UsernameTokenCallback for UsernamePasswordLoginModule 
            archive
                  .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.jboss.as.webservices.server.integration\n"))
                  .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.EJBServiceImpl.class)
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
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      JBossWSCXFTestSetup testSetup;
      testSetup = new JBossWSCXFTestSetup(UsernameAuthorizationDigestEjbTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties", getResourceFile("jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties", getResourceFile("jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties").getAbsolutePath());
      authenticationOptions.put("hashAlgorithm", "SHA");
      authenticationOptions.put("hashEncoding", "BASE64");
      authenticationOptions.put("hashCharset", "UTF-8");
      authenticationOptions.put("hashUserPassword", "false");
      authenticationOptions.put("hashStorePassword", "true");
      authenticationOptions.put("storeDigestCallback", UsernameTokenCallback.class.getName());
      authenticationOptions.put("unauthenticatedIdentity", "anonymous");
      testSetup.addSecurityDomainRequirement("JBossWS", authenticationOptions);
      return testSetup;
   }

   public void test() throws Exception
   {
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   //JBWS-3843
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

   public class TestRunner implements Callable<String>
   {
      public String call() throws Exception
      {
         URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
         setupWsse(proxy, "kermit");
         return proxy.sayHello();

      }

   }

   public void testUnauthenticated() throws Exception
   {
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
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
