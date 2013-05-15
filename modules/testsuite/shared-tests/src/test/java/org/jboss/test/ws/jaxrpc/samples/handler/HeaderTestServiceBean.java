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
package org.jboss.test.ws.jaxrpc.samples.handler;

import javax.xml.rpc.holders.StringHolder;

import org.jboss.logging.Logger;

/**
 * A service endpoint for the HeaderTestCase
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jan-2005
 */
public class HeaderTestServiceBean implements HeaderTestService
{
   // Provide logging
   private static Logger log = Logger.getLogger(HeaderTestServiceBean.class);

   public void testInHeader(String bodyMsg, String headerMsg)
   {
      log.info("testInHeader: " + bodyMsg + "," + headerMsg);
   }

   public void testInOutHeader(String bodyMsg, StringHolder headerMsg)
   {
      log.info("testInOutHeader: " + bodyMsg + "," + headerMsg.value);
      headerMsg.value += " - response";
   }
   
   public void testOutHeader(String bodyMsg, StringHolder headerMsg)
   {
      log.info("testOutHeader: " + bodyMsg + "," + headerMsg.value);
      headerMsg.value = "OUT header message";
   }
}
