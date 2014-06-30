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

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * This test is an addition to the org.jboss.test.ws.jaxws.cxf.spring.ClientSpringAppTestCase that runs
 * in forked mode as it requires setting sys props during test and hence can't be executed concurrently.
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jun-2013
 */
public final class ClientSpringAppTestCaseForked extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ClientSpringAppTestCaseForked.class, DeploymentArchives.SERVER + ", " + DeploymentArchives.CLIENT);
   }

   public void testJBossWSCXFBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testJBossWSCXFBus", Helper.class.getName()));
   }

   private String runTestInContainer(String test, String helper) throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-spring-client?path=/jaxws-cxf-spring/EndpointService&method="
            + test + "&helper=" + helper);
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
