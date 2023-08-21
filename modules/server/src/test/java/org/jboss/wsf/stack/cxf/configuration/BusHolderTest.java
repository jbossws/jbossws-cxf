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
package org.jboss.wsf.stack.cxf.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.FirstAlternativeSelector;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.jboss.ws.common.deployment.DefaultDeploymentModelFactory;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A test case for BusHolder
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Feb-2011
 * 
 */
public class BusHolderTest
{
   @Test
   public void testFirstAlternativeSelector()
   {
      final String alternative = FirstAlternativeSelector.class.getName();
      assertEquals(alternative, setupPropertyAndGetAlternativeSelector(alternative));
   }

   @Test
   public void testInvalidAlternativeSelector()
   {
      assertEquals(MaximalAlternativeSelector.class.getName(), setupPropertyAndGetAlternativeSelector("org.jboss.ws.MyInvalidAlternative"));
   }

   @Test
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
      BusHolder holder = new BusHolder(new DDBeans());
      try {
         Deployment dep = new DefaultDeploymentModelFactory().newDeployment("testDeployment", null, null);
         dep.addAttachment(SOAPAddressRewriteMetadata.class, new SOAPAddressRewriteMetadata(getTestServerConfig(), null));
         holder.configure(null, null, wsmd, dep);
         return holder.getBus().getExtension(PolicyEngine.class).getAlternativeSelector().getClass().getName();
      } finally {
         holder.close();
      }
   }
   
   private static ServerConfig getTestServerConfig() {
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

}
