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
package org.jboss.test.ws.jaxws.holder;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A JAX-WS holder test case
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class HolderTestCase extends JBossWSTest
{
   private static org.jboss.test.ws.jaxws.holder.Holder port;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-holder.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.holder.HolderServiceImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/holder/WEB-INF/web.xml"));
      return archive;
   }

   @Before
   public void setup() throws Exception
   {
      if (port == null) {
         URL wsdlURL = getResourceURL("jaxws/holder/META-INF/wsdl/HolderService.wsdl");
         QName serviceName = new QName("http://holder.jaxws.ws.test.jboss.org/", "HolderService");
         Service service = Service.create(wsdlURL, serviceName);
         port = (org.jboss.test.ws.jaxws.holder.Holder)service.getPort(org.jboss.test.ws.jaxws.holder.Holder.class);
         ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/HolderService");
      }
   }
   
   @AfterClass
   public static void cleanup() {
      port = null;
   }

   @Test
   @RunAsClient
   public void testEchoOuts() throws Exception
   {
      Holder<Integer> out1 = new Holder<Integer>();
      Holder<String> out2 = new Holder<String>();
      assertEquals(Long.valueOf(50), port.echoOuts(10, "Hello", 50L, out1, out2));
      assertEquals(Integer.valueOf(10), out1.value);
      assertEquals("Hello", out2.value);
   }

   @Test
   @RunAsClient
   public void testEchoInOuts() throws Exception
   {
      Holder<Integer> inout1 = new Holder<Integer>();
      Holder<String> inout2 = new Holder<String>();
      inout1.value = 50;
      inout2.value = "Hello";
      assertEquals(Long.valueOf(10), port.echoInOuts(10L, inout1, inout2));
      assertEquals(Integer.valueOf(50), inout1.value);
      assertEquals("Hello", inout2.value);
   }

   @Test
   @RunAsClient
   public void testEchoMixed() throws Exception
   {
      Holder<Integer> out1 = new Holder<Integer>();
      Holder<String> out2 = new Holder<String>();
      Holder<Integer> inout1 = new Holder<Integer>();
      Holder<String> inout2 = new Holder<String>();
      inout1.value = 50;
      inout2.value = "Hello2";
      assertEquals(Long.valueOf(20), port.echoMixed(30, "Hello1", inout1, inout2, 20L, out1, out2));
      assertEquals(Integer.valueOf(30), out1.value);
      assertEquals("Hello1", out2.value);
      assertEquals(Integer.valueOf(50), inout1.value);
      assertEquals("Hello2", inout2.value);
   }

   @Test
   @RunAsClient
   public void testEchoBareOut() throws Exception
   {
      Holder<String> out = new Holder<String>();
      port.echoBareOut("hi", out);
      assertEquals("hi", out.value);
   }

   @Test
   @RunAsClient
   public void testEchoBareInOut() throws Exception
   {
      Holder<String> inout = new Holder<String>();
      inout.value = "hello world!";
      port.echoBareInOut(inout);
      assertEquals("hello world!", inout.value);
   }

   @Test
   @RunAsClient
   public void testInOutAdd() throws Exception
   {
      Holder<Integer> sum = new Holder<Integer>();
      sum.value = 0;
      port.addInOut(sum, 5);
      port.addInOut(sum, 3);
      port.addInOut(sum, 4);
      assertEquals(Integer.valueOf(12), sum.value);
   }

}
