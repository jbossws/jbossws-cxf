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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * A JAX-WS holder test case
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class HolderTestCase extends JBossWSTest
{
   private org.jboss.test.ws.jaxws.holder.Holder port;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-holder.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.holder.HolderServiceImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/holder/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(HolderTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = getResourceURL("jaxws/holder/META-INF/wsdl/HolderService.wsdl");
      QName serviceName = new QName("http://holder.jaxws.ws.test.jboss.org/", "HolderService");
      Service service = Service.create(wsdlURL, serviceName);
      port = (org.jboss.test.ws.jaxws.holder.Holder)service.getPort(org.jboss.test.ws.jaxws.holder.Holder.class);
   }
   
   public void testEchoOuts() throws Exception
   {
      Holder<Integer> out1 = new Holder<Integer>();
      Holder<String> out2 = new Holder<String>();
      assertEquals(new Long(50), port.echoOuts(10, "Hello", 50L, out1, out2));
      assertEquals(new Integer(10), out1.value);
      assertEquals("Hello", out2.value);
   }
   
   public void testEchoInOuts() throws Exception
   {
      Holder<Integer> inout1 = new Holder<Integer>();
      Holder<String> inout2 = new Holder<String>();
      inout1.value = 50;
      inout2.value = "Hello";
      assertEquals(new Long(10), port.echoInOuts(10L, inout1, inout2));
      assertEquals(new Integer(50), inout1.value);
      assertEquals("Hello", inout2.value);
   }
   
   public void testEchoMixed() throws Exception
   {
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
   
   public void testEchoBareOut() throws Exception
   {
      Holder<String> out = new Holder<String>();
      port.echoBareOut("hi", out);
      assertEquals("hi", out.value);
   }

   public void testEchoBareInOut() throws Exception
   {
      Holder<String> inout = new Holder<String>();
      inout.value = "hello world!";
      port.echoBareInOut(inout);
      assertEquals("hello world!", inout.value);
   }

   public void testInOutAdd() throws Exception
   {
      Holder<Integer> sum = new Holder<Integer>();
      sum.value = 0;
      port.addInOut(sum, 5);
      port.addInOut(sum, 3);
      port.addInOut(sum, 4);
      assertEquals(new Integer(12), sum.value);
   }
}
