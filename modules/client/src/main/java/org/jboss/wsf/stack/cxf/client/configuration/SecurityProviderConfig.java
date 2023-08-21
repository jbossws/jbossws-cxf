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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.logging.Logger;

/**
 * Convenient class for setting a BouncyCastle security provider
 * through CXF interceptors when not globally available.
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Jul-2014
 */
public class SecurityProviderConfig
{
   private static Logger log = Logger.getLogger(SecurityProviderConfig.class);

   public static final boolean BC_GLOBALLY_AVAILABLE = java.security.Security.getProvider("BC") != null;
   static {
      if (BC_GLOBALLY_AVAILABLE) {
         try {
            useIvParameterSpec();
         } catch (Throwable t) {
            //ignore
            Logger.getLogger(SecurityProviderConfig.class).trace(t);
         }
      }
   }
   private static final boolean NO_LOCAL_BC = SecurityActions.getBoolean(Constants.JBWS_CXF_NO_LOCAL_BC);
   
   public static void setup(Bus bus) {
      if (!NO_LOCAL_BC && !BC_GLOBALLY_AVAILABLE) {
         if (Holder.provider != null) {
            bus.getInInterceptors().add(Holder.inInterceptor);
            bus.getOutInterceptors().add(Holder.outInterceptor);
         }
      }
   }
   
   private static class Holder {
      static final Provider provider = getBCProvider();
      static final Interceptor inInterceptor = new Interceptor(Phase.RECEIVE);
      static final Interceptor outInterceptor = new Interceptor(Phase.SETUP);
      
      private static Provider getBCProvider() {
         Provider provider = null;
         try {
            Class<?> clazz = SecurityProviderConfig.class.getClassLoader().loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
            provider = (Provider)clazz.newInstance();
            if (provider.getVersion() < 1.50) {
               useIvParameterSpec();
            }
         } catch (Throwable t) {
            Loggers.ROOT_LOGGER.cannotLoadBouncyCastleProvider(Constants.JBWS_CXF_NO_LOCAL_BC, t);
         }
         return provider;
      }
   }
   
   private static class Interceptor extends AbstractPhaseInterceptor<Message> {
      
      public Interceptor(String phase)
      {
         super(phase);
      }

      @Override
      public void handleMessage(Message message) throws Fault
      {
         Exchange exchange = message.getExchange();
         exchange.put(Provider.class, Holder.provider);
      }
   }

   private static void useIvParameterSpec() {
      // Don't override if it was set explicitly
      AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
         public Boolean run() {
            String ivParameterSpec = "org.apache.xml.security.cipher.gcm.useIvParameterSpec";
            if (System.getProperty(ivParameterSpec) == null) {
               System.setProperty(ivParameterSpec, "true");
               return false;
            }
            return true;
         }
      });
   }
}
