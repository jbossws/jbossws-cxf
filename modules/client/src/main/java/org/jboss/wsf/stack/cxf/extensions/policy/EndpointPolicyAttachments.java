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
   private Map<Placement, List<PolicyAttachment>> attachmentMap;
   
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
