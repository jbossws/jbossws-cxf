/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Security username test case
 *
 * @author alessio.soldano@jboss.com
 * @since 22-Aug-2010
 */
public final class UsernameServletTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(UsernameServletTestCase.class,"jaxws-samples-wsse-username.war, jaxws-samples-wsse-username-client.war");
   }

   public void test() throws Exception
   {
      assertEquals("1", runTestInContainer("test"));
   }
   
   public void testWrongPassword() throws Exception
   {
      assertEquals("1", runTestInContainer("testWrongPassword"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-wsse-username-client?path=/jaxws-samples-wsse-username&method=" + test
            + "&helper=" + UsernameHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
