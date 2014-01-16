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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Verifies client configuration setup (in-container tests, relying on AS model)
 *
 * @author alessio.soldano@jboss.com
 * @since 31-May-2012
 */
public class CXFClientConfigurationTestCaseForked extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(CXFClientConfigurationTestCaseForked.class, "jaxws-cxf-clientConfig.war,jaxws-cxf-clientConfig-client.jar,jaxws-cxf-clientConfig-inContainer-client.war");
   }
   
   /**
    * Verifies the default client configuration from AS model is used
    * 
    * @throws Exception
    */
   public void testDefaultClientConfigurationInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testDefaultClientConfiguration"));
   }
   
   public void testDefaultClientConfigurationOnDispatchInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testDefaultClientConfigurationOnDispatch"));
   }
   
   /**
    * Verifies a client configuration from AS model can be set
    * 
    * @throws Exception
    */
   public void testCustomClientConfigurationInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfiguration"));
   }
   
   public void testCustomClientConfigurationOnDispatchInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatch"));
   }
   
   public void testCustomClientConfigurationUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationUsingFeature"));
   }
   
   public void testCustomClientConfigurationOnDispatchUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchUsingFeature"));
   }
   
   // -------------------------
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-cxf-clientConfig-inContainer-client?path=/jaxws-cxf-clientConfig/EndpointImpl&method=" + test
            + "&helper=" + Helper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
}
