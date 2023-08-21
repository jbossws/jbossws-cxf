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
package org.jboss.wsf.stack.cxf.policy;

import java.util.List;

import org.apache.cxf.annotations.Policy.Placement;
import org.jboss.wsf.stack.cxf.extensions.policy.EndpointPolicyAttachments;
import org.jboss.wsf.stack.cxf.extensions.policy.PolicyAttachment;
import org.jboss.wsf.stack.cxf.extensions.policy.PolicyAttachmentStore;
import org.junit.Test;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * A test case of basic policy sets parsing
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Jun-2013
 * 
 */
public class PolicySetsTest
{
   @Test
   public void testStore() throws Exception {
      PolicyAttachmentStore paStore = new PolicyAttachmentStore(PolicySetsTest.class.getClassLoader());
      List<PolicyAttachment> pas = paStore.get("WS-SP-EX224_WSS11_Mutual_Auth_X509_Sign_Encrypt");
      assertNotNull(pas);
      assertEquals(3, pas.size());
      pas = paStore.get("Foo");
      assertNotNull(pas);
      assertEquals(0, pas.size());
   }
   
   @Test
   public void testPolicyAttachmentRead() throws Exception {
      PolicyAttachmentStore paStore = new PolicyAttachmentStore(PolicySetsTest.class.getClassLoader());
      List<PolicyAttachment> pas = paStore.get("WS-SP-EX224_WSS11_Mutual_Auth_X509_Sign_Encrypt");
      assertNotNull(pas);
      for (PolicyAttachment pa : pas) {
         Element el = pa.read("foo");
         assertEquals("Policy", el.getLocalName());
         assertEquals("http://www.w3.org/ns/ws-policy", el.getNamespaceURI());
      }
   }
   
   @Test
   public void testEndpointPolicyAttachments() throws Exception {
      PolicyAttachmentStore store = new PolicyAttachmentStore(PolicySetsTest.class.getClassLoader());
      EndpointPolicyAttachments epa = EndpointPolicyAttachments.newInstance(new String[]{""}, store);
      for (Placement p : Placement.values()) {
         assertEquals(0, epa.getPolicyAttachments(p).size());
      }
      epa = EndpointPolicyAttachments.newInstance(new String[]{"fafds"}, store);
      for (Placement p : Placement.values()) {
         assertEquals(0, epa.getPolicyAttachments(p).size());
      }
      epa = EndpointPolicyAttachments.newInstance(new String[]{"Foooo", "WS-SP-EX224_WSS11_Mutual_Auth_X509_Sign_Encrypt"}, store);
      for (Placement p : Placement.values()) {
         if (p == Placement.BINDING || p == Placement.BINDING_OPERATION_INPUT || p == Placement.BINDING_OPERATION_OUTPUT) {
            List<PolicyAttachment> pas = epa.getPolicyAttachments(p);
            assertNotNull(pas);
            assertEquals(1, pas.size());
            assertNotNull(pas.iterator().next().read(""));
         } else {
            assertEquals(0, epa.getPolicyAttachments(p).size());
         }
      }
   }
}