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
package org.jboss.test.ws.publish;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test WS endpoint publish api to JBoss AS container
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Jul-2011
 */
public class EndpointPublishTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(EndpointPublishTestCase.class, "endpoint-publish.war");
   }

   public void testEndpointPublish() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/endpoint-publish");
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
}
