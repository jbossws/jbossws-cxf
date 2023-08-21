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
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy sign test case
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class SignTestCase extends JBossWSTest
{
   private static final String POJO_DEPLOYMENT = "jaxws-samples-wsse-policy-sign";
   private static final String EJB3_DEPLOYMENT = "jaxws-samples-wsse-policy-sign-ejb";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = POJO_DEPLOYMENT, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, POJO_DEPLOYMENT + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = EJB3_DEPLOYMENT, testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, EJB3_DEPLOYMENT + ".jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.EJBServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF-server/bob.jks"), "bob.jks")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF-server/bob.properties"), "bob.properties")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF-server/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF-server/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF-server/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-sign-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign/META-INF/alice.properties"), "alice.properties");
         }
      });
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(POJO_DEPLOYMENT)
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(EJB3_DEPLOYMENT)
   public void testEJB() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-wsse-policy-sign-ejb/SecurityService/EJBServiceImpl?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      assertEquals("EJB Secure Hello World!", proxy.sayHello());
   }

   private void setupWsse(ServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      //workaround CXF requiring this even if no encryption is configured
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
   }
}
