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
package org.jboss.test.ws.jaxws.holder;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
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
   private org.jboss.test.ws.jaxws.holder.Holder port;

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


   protected void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = getResourceURL("jaxws/holder/META-INF/wsdl/HolderService.wsdl");
      QName serviceName = new QName("http://holder.jaxws.ws.test.jboss.org/", "HolderService");
      Service service = Service.create(wsdlURL, serviceName);
      port = (org.jboss.test.ws.jaxws.holder.Holder)service.getPort(org.jboss.test.ws.jaxws.holder.Holder.class);
      ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/HolderService");
   }

   @Test
   @RunAsClient
   public void testEchoOuts() throws Exception
   {
      setUp();
      Holder<Integer> out1 = new Holder<Integer>();
      Holder<String> out2 = new Holder<String>();
      assertEquals(new Long(50), port.echoOuts(10, "Hello", 50L, out1, out2));
      assertEquals(new Integer(10), out1.value);
      assertEquals("Hello", out2.value);
   }

   @Test
   @RunAsClient
   public void testEchoInOuts() throws Exception
   {
      setUp();
      Holder<Integer> inout1 = new Holder<Integer>();
      Holder<String> inout2 = new Holder<String>();
      inout1.value = 50;
      inout2.value = "Hello";
      assertEquals(new Long(10), port.echoInOuts(10L, inout1, inout2));
      assertEquals(new Integer(50), inout1.value);
      assertEquals("Hello", inout2.value);
   }

   @Test
   @RunAsClient
   public void testEchoMixed() throws Exception
   {
      setUp();
      Holder<Integer> out1 = new Holder<Integer>();
      Holder<String> out2 = new Holder<String>();
      Holder<Integer> inout1 = new Holder<Integer>();
      Holder<String> inout2 = new Holder<String>();
      inout1.value = 50;
      inout2.value = "Hello2";
      assertEquals(new Long(20), port.echoMixed(30, "Hello1", inout1, inout2, 20L, out1, out2));
      assertEquals(new Integer(30), out1.value);
      assertEquals("Hello1", out2.value);
      assertEquals(new Integer(50), inout1.value);
      assertEquals("Hello2", inout2.value);
   }

   @Test
   @RunAsClient
   public void testEchoBareOut() throws Exception
   {
      setUp();
      Holder<String> out = new Holder<String>();
      port.echoBareOut("hi", out);
      assertEquals("hi", out.value);
   }

   @Test
   @RunAsClient
   public void testEchoBareInOut() throws Exception
   {
      setUp();
      Holder<String> inout = new Holder<String>();
      inout.value = "hello world!";
      port.echoBareInOut(inout);
      assertEquals("hello world!", inout.value);
   }

   @Test
   @RunAsClient
   public void testInOutAdd() throws Exception
   {
      setUp();
      Holder<Integer> sum = new Holder<Integer>();
      sum.value = 0;
      port.addInOut(sum, 5);
      port.addInOut(sum, 3);
      port.addInOut(sum, 4);
      assertEquals(new Integer(12), sum.value);
   }

}
