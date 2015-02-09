/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wssePolicy;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security Policy sign & encrypt test case
 *
 * @author alessio.soldano@jboss.com
 * @since 1-May-2009
 */
@RunWith(Arquillian.class)
public final class SignEncryptTestCase extends JBossWSTest
{
   @Deployment(name="jaxws-samples-wssePolicy-sign-encrypt", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wssePolicy-sign-encrypt.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.KeystorePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.jaxws.SayHelloResponse.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/bob.jks"), "bob.jks")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/bob.properties"), "bob.properties")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-wssePolicy-sign-encrypt?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      try
      {
         assertEquals("Secure Hello World!", proxy.sayHello());
      }
      catch (SOAPFaultException e)
      {
         throw new Exception("Error " + e.getMessage() + " - please check that the Bouncy Castle provider is installed.", e);
      }
   }

   private void setupWsse(ServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
   }
   
   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wssePolicy-sign-encrypt-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/sign-encrypt/META-INF/alice.properties"), "alice.properties");
         }
      });
   }
}
