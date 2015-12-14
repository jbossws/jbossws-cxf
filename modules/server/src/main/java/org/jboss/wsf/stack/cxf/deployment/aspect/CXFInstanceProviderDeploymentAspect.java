/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
