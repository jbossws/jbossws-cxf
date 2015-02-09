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
package org.jboss.test.ws.jaxws.cxf.jbws3648;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 *
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2013
 */
@RunWith(Arquillian.class)
public class PolicyAttachmentWSDLTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3648.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointTwo.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3648.EndpointTwoImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointOneWSDL() throws Exception {
      URL wsdlURL = new URL(baseURL + "/ServiceOne?wsdl");
      checkPolicyAttachments(wsdlURL, new String[]{"WS-RM_Policy_spec_example",
            "WS-SP-EX223_binding_policy",
            "WS-SP-EX223_Binding_Operation_Input_Policy",
            "WS-SP-EX223_Binding_Operation_Output_Policy",
            "WS-Addressing_binding_policy"});
   }
   
   @Test
   @RunAsClient
   public void testEndpointTwoWSDL() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/ServiceTwo?wsdl");
      checkPolicyAttachments(wsdlURL, new String[]{"WS-RM_Policy_spec_example",
            "WS-SP-EX223_binding_policy",
            "WS-SP-EX223_Binding_Operation_Input_Policy",
            "WS-SP-EX223_Binding_Operation_Output_Policy",
            "WS-Addressing_binding_policy"});
   }
   
   private void checkPolicyAttachments(URL wsdlURL, String[] refIds) throws Exception {
      final String wsdl = IOUtils.readAndCloseStream(wsdlURL.openStream());
      for (String refId : refIds) {
         assertTrue("WSDL does not contain '" + refId + "'", wsdl.contains(refId));
      }
   }
   
}
