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
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy username test case (using secure transport)
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class UsernameOverTransportTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-username.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServerUsernamePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/username/WEB-INF/web.xml"));
      return archive;
   }
   
   /** System properties - currently set at testsuite start time 
   System.setProperty("javax.net.ssl.trustStore", "my.truststore");
   System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
   System.setProperty("javax.net.ssl.trustStoreType", "jks");
   System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
   */
   
   @Test
   @RunAsClient
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = getServiceWSDLURL();
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   public void testWrongPassword() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = getServiceWSDLURL();
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "snoopy");
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
   
   private URL getServiceWSDLURL() throws Exception {
      final int offset = baseURL.getPort() - 8080;
      return new URL("https", baseURL.getHost(), (8443 + offset), "/jaxws-samples-wsse-policy-username?wsdl");
   }

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.UsernamePasswordCallback");
   }
}
