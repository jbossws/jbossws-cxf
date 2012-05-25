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
package org.jboss.wsf.stack.cxf.configuration;

import junit.framework.TestCase;

import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.FirstAlternativeSelector;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentState;
import org.jboss.wsf.spi.deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;


/**
 * A test case for BusHolder
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Feb-2011
 * 
 */
public class BusHolderTest extends TestCase
{
   public void testFirstAlternativeSelector()
   {
      final String alternative = FirstAlternativeSelector.class.getName();
      assertEquals(alternative, setupPropertyAndGetAlternativeSelector(alternative));
   }
   
   public void testInvalidAlternativeSelector()
   {
      assertEquals(MaximalAlternativeSelector.class.getName(), setupPropertyAndGetAlternativeSelector("org.jboss.ws.MyInvalidAlternative"));
   }
   
   public void testDefaultAlternativeSelector()
   {
      assertEquals(MaximalAlternativeSelector.class.getName(), setupPropertyAndGetAlternativeSelector(null));
   }
   
   private static String setupPropertyAndGetAlternativeSelector(String alternative) {
      Deployment dep = new TestDeployment();
      if (alternative != null) {
         JBossWebservicesMetaData wsmd = new JBossWebservicesMetaData(null);
         wsmd.setProperty(Constants.CXF_POLICY_ALTERNATIVE_SELECTOR_PROP, alternative);
         dep.addAttachment(JBossWebservicesMetaData.class, wsmd);
      }
      BusHolder holder = new NonSpringBusHolder(new DDBeans());
      try {
         holder.configure(null, null, null, dep);
         return holder.getBus().getExtension(PolicyEngine.class).getAlternativeSelector().getClass().getName();
      } finally {
         holder.close();
      }
   }
   
   private static class TestDeployment extends AbstractExtensible implements Deployment {

      @Override
      public String getSimpleName()
      {
         return null;
      }

      @Override
      public void setSimpleName(String name)
      {
      }

      @Override
      public ClassLoader getInitialClassLoader()
      {
         return null;
      }

      @Override
      public void setInitialClassLoader(ClassLoader loader)
      {
      }

      @Override
      public ClassLoader getRuntimeClassLoader()
      {
         return null;
      }

      @Override
      public void setRuntimeClassLoader(ClassLoader loader)
      {
      }

      @Override
      public DeploymentType getType()
      {
         return null;
      }

      @Override
      public void setType(DeploymentType type)
      {
      }

      @Override
      public DeploymentState getState()
      {
         return null;
      }

      @Override
      public void setState(DeploymentState type)
      {
      }

      @Override
      public Service getService()
      {
         return null;
      }

      @Override
      public void setService(Service service)
      {
      }
      
   }

}
