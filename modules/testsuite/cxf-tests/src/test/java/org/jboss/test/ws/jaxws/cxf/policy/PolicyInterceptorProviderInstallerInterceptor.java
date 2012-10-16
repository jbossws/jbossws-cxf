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
