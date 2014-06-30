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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * A test case that verifies Bus references do not leak into servlet clients 
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Mar-2011
 *
 */
public class ServletClientBusTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-cxf-bus-servlet-client";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ServletClientBusTestCase.class, DeploymentArchives.SERVER);
   }
   
   public void testSingleDeploy() throws Exception
   {
      deploy(DeploymentArchives.SERVLET_CLIENT);
      try
      {
         URL url = new URL(TARGET_ENDPOINT_ADDRESS + "?method=testBusCreation");
         assertEquals("OK testBusCreation", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(TARGET_ENDPOINT_ADDRESS + "?method=testSOAPConnection&host=" + getServerHost());
         assertEquals("OK testSOAPConnection", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(TARGET_ENDPOINT_ADDRESS + "?method=testWebServiceRef");
         assertEquals("OK testWebServiceRef", IOUtils.readAndCloseStream(url.openStream()));
         
         url = new URL(TARGET_ENDPOINT_ADDRESS + "?method=testWebServiceClient&host=" + getServerHost());
         assertEquals("OK testWebServiceClient", IOUtils.readAndCloseStream(url.openStream()));
      }
      finally
      {
         undeploy(DeploymentArchives.SERVLET_CLIENT);
      }
   }
}
