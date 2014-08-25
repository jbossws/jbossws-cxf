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
package org.jboss.wsf.stack.cxf.client;

import static org.jboss.wsf.stack.cxf.client.Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.NEW_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.TCCL_BUS_STRATEGY;
import static org.jboss.wsf.stack.cxf.client.Constants.THREAD_BUS_STRATEGY;

import javax.xml.ws.WebServiceFeature;

import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;

/**
 * A class selecting the proper bus to be used for creating a
 * JAXWS client.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2013
 *
 */
public abstract class ClientBusSelector
{
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
   }
   
   public static String selectStrategy(WebServiceFeature... features) {
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
   
   public static String getDefaultStrategy() {
      return sysPropStrategy;
   }
}
