/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.client.Constants;
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
