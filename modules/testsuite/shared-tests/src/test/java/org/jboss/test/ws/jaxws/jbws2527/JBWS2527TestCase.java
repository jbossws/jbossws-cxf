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
package org.jboss.test.ws.jaxws.jbws2527;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * JBWS-2527 testcase: BeanFactory not initialized or already closed
 * 
 * @author richard.opalka@jboss.com
 */
public class JBWS2527TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2527TestCase.class, "", true);
   }

   public void test() throws Exception
   {
      for (int i = 0; i < 2; i++)
      {
         executeTest();
         executeTest();
      }
   }

   public void executeTest() throws Exception
   {
      try
      {
         deploy("jaxws-jbws2527-service.war");
         deploy("jaxws-jbws2527-client.war");

         assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":8080/jaxws-jbws2527-client/jbws2527").openStream()));
      }
      finally
      {
         undeploy("jaxws-jbws2527-client.war");
         undeploy("jaxws-jbws2527-service.war");
      }
   }
}
