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
package org.jboss.wsf.stack.cxf.client;

import static org.jboss.wsf.stack.cxf.client.Constants.JBWS_CXF_JAXWS_CLIENT_BUS_SELECTOR;
import static org.jboss.wsf.stack.cxf.client.Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.NEW_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.TCCL_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.THREAD_BUS_STRATEGY;

import jakarta.xml.ws.WebServiceFeature;

import org.apache.cxf.Bus;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.wsf.stack.cxf.i18n.Messages;

/**
 * A class selecting the proper bus to be used for creating a
 * JAXWS client.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2013
 *
 */
public class ClientBusSelector
{
   private static final ClientBusSelector instance;
   private static final String sysPropStrategy;
   static {
      final String propValue = SecurityActions.getSystemProperty(JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY, ClassLoaderProvider.isSet() ? TCCL_BUS_STRATEGY : THREAD_BUS_STRATEGY);
      if (THREAD_BUS_STRATEGY.equals(propValue) || NEW_BUS_STRATEGY.equals(propValue) || TCCL_BUS_STRATEGY.equals(propValue))
      {
         sysPropStrategy = propValue;
      }
      else
      {
         Loggers.ROOT_LOGGER.unknownJAXWSClientBusStrategy(propValue);
         sysPropStrategy = THREAD_BUS_STRATEGY;
      }
      final String selectorPropValue = SecurityActions.getSystemProperty(JBWS_CXF_JAXWS_CLIENT_BUS_SELECTOR, ClientBusSelector.class.getName());
      ClientBusSelector cbs;
      try {
         Class<?> clazz = Class.forName(selectorPropValue);
         cbs = (ClientBusSelector)clazz.newInstance();
      } catch (Exception e) {
         Loggers.ROOT_LOGGER.couldNotLoadClientBusSelector(selectorPropValue, e);
         cbs = new ClientBusSelector();
      }
      instance = cbs;
   }
   
   public static ClientBusSelector getInstance() {
      return instance;
   }
   
   public String selectStrategy(WebServiceFeature... features) {
      boolean createNewBus = false;
      boolean tcclBoundBus = false;
      boolean threadBus = false;
      int count = 0;
      if (features != null)
      {
         for (WebServiceFeature f : features)
         {
            final String className = f.getClass().getName();
            if (UseNewBusFeature.class.getName().equals(className))
            {
               createNewBus = f.isEnabled();
               count++;
            }
            else if (UseTCCLBusFeature.class.getName().equals(className))
            {
               tcclBoundBus = f.isEnabled();
               count++;
            }
            else if (UseThreadBusFeature.class.getName().equals(className))
            {
               threadBus = f.isEnabled();
               count++;
            }
         }
      }
      if (count > 1)
      {
         throw Messages.MESSAGES.incompatibleJAXWSClientBusFeatureProvided();
      }
      
      String featureStrategy = null;
      if (createNewBus)
      {
         featureStrategy = NEW_BUS_STRATEGY;
      }
      else if (tcclBoundBus)
      {
         featureStrategy = TCCL_BUS_STRATEGY;
      }
      else if (threadBus)
      {
         featureStrategy = THREAD_BUS_STRATEGY;
      }
      return featureStrategy != null ? featureStrategy : sysPropStrategy;
   }
   
   public Bus createNewBus() {
      return new JBossWSBusFactory().createBus();
   }
   
   public static String getDefaultStrategy() {
      return sysPropStrategy;
   }
}
