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

import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 * Test CXF logging on the client side
 *
 * @author alessio.soldano@jboss.com
 * @since 09-Jun-2010
 */
public class CXFLoggingTestCase extends JBossWSTest
{
   @Test
   public void testLogging() throws Exception
   {
      Logger log = LogUtils.getL7dLogger(CXFLoggingTestCase.class);

      //After CXF 3.4.0 , the logg4j is removed:https://issues.apache.org/jira/browse/CXF-8264
      //It now uses j.u.l.Logger
      assertTrue("Expected an instance of java.util.logging.Logger , but it is " + log.getClass().getName(),
              log instanceof Logger);
   }
}
