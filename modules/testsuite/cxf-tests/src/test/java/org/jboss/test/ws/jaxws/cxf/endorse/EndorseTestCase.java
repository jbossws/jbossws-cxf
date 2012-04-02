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
package org.jboss.test.ws.jaxws.cxf.endorse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test required endorsing when using the CXF stack
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Jun-2010
 */
public class EndorseTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(EndorseTestCase.class, "jaxws-cxf-endorse.war,jaxws-cxf-endorse-no-export.war");
   }
   
   public void testClientSide()
   {
      Helper.verifyCXF();
   }

   public void testServerSide() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-cxf-endorse?provider=" + ProviderImpl.class.getName()));
   }
   
   public void testServerSideNoExport() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-cxf-endorse-no-export?provider=" + ProviderImpl.class.getName()));
   }
   
   private static void runServerTest(URL url) throws Exception {
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      String retStr = br.readLine();
      assertEquals("OK", retStr);
   }
}
