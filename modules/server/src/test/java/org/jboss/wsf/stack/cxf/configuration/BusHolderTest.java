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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.FirstAlternativeSelector;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.jboss.ws.common.deployment.DefaultDeploymentModelFactory;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.management.ServerConfig;
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
      JBossWebservicesMetaData wsmd = null;
      if (alternative != null) {
         Map<String, String> props = new HashMap<String, String>();
         props.put(Constants.CXF_POLICY_ALTERNATIVE_SELECTOR_PROP, alternative);
         wsmd = new JBossWebservicesMetaData(null, null, null, null, props, null, null);
      }
      BusHolder holder = new NonSpringBusHolder(new DDBeans()) {
         protected ServerConfig getServerConfig() {
            return new AbstractServerConfig()
            {
               @Override
               public File getServerTempDir()
               {
                  // TODO Auto-generated method stub
                  return null;
               }
               @Override
               public File getServerDataDir()
               {
                  // TODO Auto-generated method stub
                  return null;
               }
               @Override
               public File getHomeDir()
               {
                  // TODO Auto-generated method stub
                  return null;
               }
            };
         }
      };
      try {
         holder.configure(null, null, wsmd, new DefaultDeploymentModelFactory().newDeployment("testDeployment", null, null));
         return holder.getBus().getExtension(PolicyEngine.class).getAlternativeSelector().getClass().getName();
      } finally {
         holder.close();
      }
   }

}
