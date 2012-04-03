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
package org.jboss.test.ws.jaxws.cxf.spring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * An application embedding Spring jars acts as a client to an existing WS endpoint.
 * This testcase verifies the spring availability in the app does not badly affect ws functionalities. 
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2012
 */
public final class ClientSpringAppTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ClientSpringAppTestCase.class, "jaxws-cxf-spring-client.war, jaxws-cxf-spring.war");
   }

   public void testSpringAvailability() throws Exception
   {
      assertEquals("1", runTestInContainer("testSpringAvailability", Helper.class.getName()));
   }

   public void testJBossWSCXFBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testJBossWSCXFBus", Helper.class.getName()));
   }

   public void testJBossWSCXFSpringBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testJBossWSCXFSpringBus", Helper.class.getName()));
   }

   public void testJAXWSClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testJAXWSClient", Helper.class.getName()));
   }

   public void testSpringFunctionalities() throws Exception
   {
      assertEquals("1", runTestInContainer("testSpringFunctionalities", Helper.class.getName()));
   }

   private String runTestInContainer(String test, String helper) throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-spring-client?path=/jaxws-cxf-spring&method="
            + test + "&helper=" + helper);
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
}
