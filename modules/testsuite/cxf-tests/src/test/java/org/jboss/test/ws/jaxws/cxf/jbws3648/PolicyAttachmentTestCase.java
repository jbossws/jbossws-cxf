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
package org.jboss.test.ws.jaxws.cxf.jbws3648;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.CryptoHelper;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Jun-2013
 */
@RunWith(Arquillian.class)
public class PolicyAttachmentTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3648-b.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.apache.ws.security\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointFour.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointFourImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointThree.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointThreeImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.KeystorePasswordCallback.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/WEB-INF/bob.jks"), "classes/bob.jks")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/WEB-INF/bob.properties"), "classes/bob.properties")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/policies/My-WSSE-conf-BINDING.xml"),
                  "classes/META-INF/policies/My-WSSE-conf-BINDING.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/policies/org.jboss.wsf.stack.cxf.extensions.policy.PolicyAttachmentStore"),
                  "classes/META-INF/policies/org.jboss.wsf.stack.cxf.extensions.policy.PolicyAttachmentStore");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3648-b-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3648-b/META-INF/alice.properties"), "alice.properties");
         }
      });
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testEndpointWithWSSEAndWSA() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(bos);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pw));
         
         URL wsdlURL = new URL(baseURL + "/ServiceThree?wsdl");
         QName serviceName = new QName("http://org.jboss.ws.jaxws.cxf/jbws3648", "ServiceThree");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         EndpointThree proxy = (EndpointThree)service.getPort(EndpointThree.class);
         setupWsse((BindingProvider)proxy);
         
         assertEquals("Foo3", proxy.echo("Foo3"));
         final String m = bos.toString();
         assertTrue("WS-Addressing was not enabled!", m.contains("http://www.w3.org/2005/08/addressing") && m.contains("http://www.w3.org/2005/08/addressing/anonymous"));
         assertTrue("WS-Security was not enabled!", m.contains("http://www.w3.org/2001/04/xmlenc#rsa-1_5") && m.contains("http://www.w3.org/2001/04/xmlenc#tripledes-cbc"));
      } finally {
         bus.shutdown(true);
         pw.close();
         bos.close();
      }
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testEndpointWithCustomWSSEAndWSA() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(bos);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pw));
         
         URL wsdlURL = new URL(baseURL + "/ServiceFour?wsdl");
         QName serviceName = new QName("http://org.jboss.ws.jaxws.cxf/jbws3648", "ServiceFour");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         EndpointFour proxy = (EndpointFour)service.getPort(EndpointFour.class);
         setupWsse((BindingProvider)proxy);
         
         try {
            assertEquals("Foo4", proxy.echo("Foo4"));
         } catch (Exception e) {
            throw CryptoHelper.checkAndWrapException(e);
         }
         final String m = bos.toString();
         assertTrue("WS-Addressing was not enabled!", m.contains("http://www.w3.org/2005/08/addressing") && m.contains("http://www.w3.org/2005/08/addressing/anonymous"));
         assertTrue("WS-Security was not enabled!", m.contains("http://www.w3.org/2001/04/xmlenc#rsa-1_5") && m.contains("http://www.w3.org/2001/04/xmlenc#aes256-cbc"));
      } finally {
         bus.shutdown(true);
         pw.close();
         bos.close();
      }
   }
   
   private void setupWsse(BindingProvider proxy)
   {
      proxy.getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      proxy.getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      proxy.getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      proxy.getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      proxy.getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      proxy.getRequestContext().put(Message.RECEIVE_TIMEOUT, 120000);
   }
}
