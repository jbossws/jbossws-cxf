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
package org.jboss.test.ws.jaxws.cxf.policy;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.IgnorablePolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.policy.PolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;

/**
 * An interceptor that installs the IgnorablePolicyInterceptorProvider into the PolicyInterceptorProviderRegistry.
 * That (a) makes CXF  believe it has implementation for the custom/unknown policy and (b) causes the custom/unknown
 * policy assertion to be marked as asserted (by the IgnorableAssertionsInterceptor installed by the provider).
 * 
 * @author alessio.soldano@jboss.com
 *
 */
public class PolicyInterceptorProviderInstallerInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static QName customPolicyQname = new QName("http://my.custom.org/policy", "MyPolicy");

   public PolicyInterceptorProviderInstallerInterceptor()
   {
      super(Phase.RECEIVE);
      addBefore(PolicyInInterceptor.class.getName());
   }

   public void handleMessage(Message message) throws Fault
   {
      PolicyInterceptorProviderRegistry reg = message.getExchange().getBus()
            .getExtension(PolicyInterceptorProviderRegistry.class);

      Set<PolicyInterceptorProvider> set = reg.get(customPolicyQname);
      if (set == null || set.isEmpty())
      {
         set = new HashSet<PolicyInterceptorProvider>();
         set.add(new IgnorablePolicyInterceptorProvider(customPolicyQname));
         reg.register(customPolicyQname, set);
      }
   }
}
