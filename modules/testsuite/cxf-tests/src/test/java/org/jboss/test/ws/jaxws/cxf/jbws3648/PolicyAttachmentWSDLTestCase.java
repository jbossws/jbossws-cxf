/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
