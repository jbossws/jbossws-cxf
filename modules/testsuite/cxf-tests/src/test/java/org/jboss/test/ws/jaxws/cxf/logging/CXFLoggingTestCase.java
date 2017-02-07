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

import org.apache.cxf.common.logging.Log4jLogger;
import org.apache.cxf.common.logging.LogUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 * Test CXF logging on the client side uses Log4J 
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
      
      assertTrue("Expected an instance of " + Log4jLogger.class, log instanceof Log4jLogger);
   }
}
