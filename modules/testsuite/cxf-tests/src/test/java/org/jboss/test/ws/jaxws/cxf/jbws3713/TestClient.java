/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.net.URL;

public class TestClient
{
   public static void main(String[] args) throws Exception
   {
      final String wsdlAddress = args[0];
      final String threadPoolSize = args[1];
      final String invocations = args[2];
      int ret1 = new HelperUsignThreadLocal().run(new URL(wsdlAddress), Integer.parseInt(threadPoolSize), Integer.parseInt(invocations));
      int ret2 = new Helper().run(new URL(wsdlAddress), Integer.parseInt(threadPoolSize), Integer.parseInt(invocations));
      System.out.println(String.valueOf(ret1) + " " + String.valueOf(ret2));
   }
}
