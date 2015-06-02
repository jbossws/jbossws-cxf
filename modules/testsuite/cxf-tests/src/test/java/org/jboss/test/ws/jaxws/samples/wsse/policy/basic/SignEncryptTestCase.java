/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy sign & encrypt test case
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class SignEncryptTestCase extends JBossWSTest
{
   private static final String WS_DEPLOYMENT = "jaxws-samples-wsse-policy-sign-encrypt";
   private static final String SERVLET_DEPLOYMENT = "jaxws-samples-wsse-policy-sign-encrypt-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = WS_DEPLOYMENT, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WS_DEPLOYMENT + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/bob.jks"), "classes/bob.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/bob.properties"), "classes/bob.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = SERVLET_DEPLOYMENT, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-policy-sign-encrypt-client.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.basic.SignEncryptHelper.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.CryptoHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.properties"), "classes/META-INF/alice.properties")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/alice.jks"), "classes/META-INF/alice.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/jaxws-client-config.xml"), "classes/META-INF/jaxws-client-config.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/basic/sign-encrypt/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("SignEncryptTestCase-client.jar") { {
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
   @WrapThreadContextClassLoader
   @OperateOnDeployment(WS_DEPLOYMENT)
   public void testClientSide() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(baseURL + "/jaxws-samples-wsse-policy-sign-encrypt");
      assertTrue(helper.testSignEncrypt());
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(WS_DEPLOYMENT)
   public void testClientSideUsingConfigProperties() throws Exception
   {
      SignEncryptHelper helper = new SignEncryptHelper();
      helper.setTargetEndpoint(baseURL + "/jaxws-samples-wsse-policy-sign-encrypt");
      assertTrue(helper.testSignEncryptUsingConfigProperties());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVLET_DEPLOYMENT)
   public void testServerSide() throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-samples-wsse-policy-sign-encrypt&method=testSignEncrypt&helper=" + SignEncryptHelper.class.getName());
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVLET_DEPLOYMENT)
   public void testServerSideUsingConfigProperties() throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-samples-wsse-policy-sign-encrypt&method=testSignEncryptUsingConfigProperties&helper=" + SignEncryptHelper.class.getName());
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
}
