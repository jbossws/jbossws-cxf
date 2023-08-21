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
package org.jboss.wsf.stack.cxf.deployment.aspect;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.stack.cxf.CXFInstanceProvider;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;


/**
 * Instance provider DA.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class CXFInstanceProviderDeploymentAspect extends AbstractDeploymentAspect
{
    @Override
    public void start(final Deployment dep)
    {
       for (final Endpoint ep : dep.getService().getEndpoints())
       {
          final Object serviceBean = ep.getAttachment(Object.class);
          org.apache.cxf.endpoint.Endpoint cxfEp = ep.getAttachment(org.apache.cxf.endpoint.Endpoint.class);
          ep.setInstanceProvider(new CXFInstanceProvider(serviceBean, cxfEp));
       }
       setUserEndpointBus(dep);
    }
    
    private void setUserEndpointBus(final Deployment dep) {
       //first make sure the global default bus is built with visibility over client dependencies only
       final ClassLoader clientClassLoader = ProviderImpl.class.getClassLoader();
       if (BusFactory.getDefaultBus(false) == null)
       {
          JBossWSBusFactory.getDefaultBus(clientClassLoader);
       }
       final ClassLoader cl = SecurityActions.getContextClassLoader();
       try {
          //then set the TCCL to a delegate classloader adding jbws client integration to user deployment dependencies
          SecurityActions.setContextClassLoader(createDelegateClassLoader(clientClassLoader, dep.getClassLoader()));
          final Bus userBus = BusFactory.newInstance().createBus();
          //finally, create a new Bus instance to be later assigned to the thread running the user endpoint business methods
          for (final Endpoint ep : dep.getService().getEndpoints()) {
             ep.addAttachment(Bus.class, userBus);
          }
       } finally {
          SecurityActions.setContextClassLoader(cl);
       }
    }
    
    private static DelegateClassLoader createDelegateClassLoader(final ClassLoader clientClassLoader, final ClassLoader origClassLoader)
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm == null)
       {
          return new DelegateClassLoader(clientClassLoader, origClassLoader);
       }
       else
       {
          return AccessController.doPrivileged(new PrivilegedAction<DelegateClassLoader>()
          {
             public DelegateClassLoader run()
             {
                return new DelegateClassLoader(clientClassLoader, origClassLoader);
             }
          });
       }
    }
}
