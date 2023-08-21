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
package org.jboss.wsf.stack.cxf.extensions.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.annotations.Policy.Placement;


/**
 * A class for collecting policy attachments for a given endpoint
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Jun-2013
 *
 */
public class EndpointPolicyAttachments
{
   private final Map<Placement, List<PolicyAttachment>> attachmentMap;
   
   private EndpointPolicyAttachments(Map<Placement, List<PolicyAttachment>> attachmentMap) {
      this.attachmentMap = attachmentMap;
   }
   
   public static EndpointPolicyAttachments newInstance(String[] sets, PolicyAttachmentStore store) {
      Map<Placement, List<PolicyAttachment>> map = new HashMap<Placement, List<PolicyAttachment>>();
      for (String set : sets) {
         List<PolicyAttachment> attachments = store.get(set);
         for (PolicyAttachment attachment: attachments) {
            Placement p = attachment.getPlacement();
            if (map.containsKey(p)) {
               map.get(p).add(attachment);
            } else {
               List<PolicyAttachment> list = new ArrayList<PolicyAttachment>(4);
               list.add(attachment);
               map.put(attachment.getPlacement(), list);
            }
         }
      }
      return new EndpointPolicyAttachments(map);
   }
   
   public List<PolicyAttachment> getPolicyAttachments(Placement placement)
   {
      List<PolicyAttachment> pal = attachmentMap.get(placement);
      if (pal == null) {
         return Collections.emptyList();
      } else {
         return pal;
      }
   }
}
