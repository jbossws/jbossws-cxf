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
package org.jboss.wsf.stack.cxf.policy;

import java.util.List;

import junit.framework.TestCase;

import org.apache.cxf.annotations.Policy.Placement;
import org.junit.Test;
import org.w3c.dom.Element;


/**
 * A test case of basic policy sets parsing
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Jun-2013
 * 
 */
public class PolicySetsTest extends TestCase
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