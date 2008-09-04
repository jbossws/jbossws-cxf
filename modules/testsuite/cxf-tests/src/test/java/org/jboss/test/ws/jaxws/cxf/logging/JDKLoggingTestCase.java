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
package org.jboss.test.ws.jaxws.cxf.logging;

import java.util.logging.Handler;
import java.util.logging.Logger;

import org.jboss.wsf.common.logging.JDKLogHandler;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test redirection of JDK logging on the client side 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2007
 */
public class JDKLoggingTestCase extends JBossWSTest
{
   public void testLogging() throws Exception
   {
      Logger log = Logger.getLogger(JDKLoggingTestCase.class.getName());
      assertHandlers(log);

      log = Logger.getLogger(JDKLoggingTestCase.class.getName());
      assertHandlers(log);

      log.info("test message");
   }

   private void assertHandlers(Logger log)
   {
      int found = 0;
      for (Handler handler : log.getHandlers())
      {
         if (handler instanceof JDKLogHandler)
            found++;
      }
      assertEquals("Expected one jboss handler", 1, found);
   }
}
