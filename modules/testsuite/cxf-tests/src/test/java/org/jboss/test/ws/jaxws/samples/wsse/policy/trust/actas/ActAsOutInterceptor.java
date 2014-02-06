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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas;

import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.message.Message;
import org.apache.cxf.interceptor.Fault;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * User: rsearls@redhat.com
 * Date: 1/26/14
 */
public class ActAsOutInterceptor extends AbstractPhaseInterceptor<Message> {

    public ActAsOutInterceptor () {
        super(Phase.SETUP);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
   dump(message);
        String tmpStr ="<wsse:UsernameToken  xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"id-myactaskey\"> <wsse:Username>myactaskey</wsse:Username> </wsse:UsernameToken>";
       message.put(SecurityConstants.STS_TOKEN_ACT_AS, tmpStr);
    }

   private void dump(Message message){
      Set<String> keyList = message.keySet();
      for(String key: keyList){
         Object o = message.get(key);
         if(o instanceof String){
            System.out.println("##ACTAS key: " + key + "  value: " + (String)o);
         } else if (o instanceof org.apache.cxf.service.model.MessageInfo) {
            dumpMsgInfo(key, (org.apache.cxf.service.model.MessageInfo)o);

         } else if (o instanceof org.apache.cxf.service.model.BindingMessageInfo) {
            org.apache.cxf.service.model.BindingMessageInfo bMsgInfo = (org.apache.cxf.service.model.BindingMessageInfo)o;
            org.apache.cxf.service.model.MessageInfo mInfo = bMsgInfo.getMessageInfo();
            dumpMsgInfo(key, mInfo);

         } else {
               System.out.println("##ACTAS key: " + key + "  value++: " + o.getClass().getCanonicalName());

         }
      }

   }


   private void dumpMsgInfo(String key, org.apache.cxf.service.model.MessageInfo mInfo){
      if (mInfo.getProperties() == null){
         System.out.println("##ACTAS key: " + key + "  value--: " + mInfo.getClass().getCanonicalName());
      } else {
         dumpMap(mInfo.getProperties());
      }
      if (mInfo.getDocumentation() == null){
         System.out.println("##ACTAS msgInfo doc is NULL ");
      } else {
         System.out.println("##ACTAS msgInfo doc: " + mInfo.getDocumentation());
      }
   }

   private void dumpMap(Map<String, Object> pMap){

      Set<String> mList = pMap.keySet();
      for(String key: mList){
         Object o = pMap.get(key);
         if(o instanceof String){
            System.out.println("--##ACTAS map key: " + key + "  value: " + (String)o);
         } else {
            System.out.println("--##ACTAS map key: " + key + "  value: " + o.getClass().getCanonicalName());
         }
      }
   }

    @Override
    public void handleFault(Message message) {
    }
}
