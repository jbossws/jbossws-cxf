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

import java.util.List;

import junit.framework.Test;

import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.test.JBossWSTestSetup;

public class UseTCCLBusStrategyTestCase extends ClientBusStrategyTests
{
   public static Test suite()
   {
      return new JBossWSTestSetup(UseTCCLBusStrategyTestCase.class, DeploymentArchives.SERVER);
   }

   public void testClientWithTCCLBusStrategy() throws Exception
   {
      final int threadPoolSize = 4;
      final int invocations = 5;
      List<Integer> list = runJBossModulesClient(Constants.TCCL_BUS_STRATEGY, endpointAddress + "?wsdl", threadPoolSize, invocations);
      assertEquals(1, list.get(0).intValue());
      assertEquals(1, list.get(1).intValue());
   }
}
