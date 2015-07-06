/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.config;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
/**
 * Interceptor to set runtime configuration with http put url like: http://localhost:8080/context/endpoint?statistics-enabled=true 
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
@NoJSR250Annotations(unlessNull = "bus")
public class RuntimeConfigListenerManagerImpl implements RuntimeConfigListenerManager
{ 

      private final ConcurrentHashMap<String, RuntimeConfigListener> listeners;
      private Bus bus;
      
      public RuntimeConfigListenerManagerImpl() {
          listeners = new ConcurrentHashMap<String, RuntimeConfigListener>();
      }
      public RuntimeConfigListenerManagerImpl(Bus b) {
          listeners = new ConcurrentHashMap<String, RuntimeConfigListener>();
          setBus(b);
      }
      
      @Resource
      public final void setBus(Bus b) {
          bus = b;
          if (null != bus) {
              bus.setExtension(this, RuntimeConfigListenerManager.class);
          }
      }
      
      @Override
      public void registerListener(String config, RuntimeConfigListener listener)
      {
         listeners.putIfAbsent(config, listener);
         
      }
      @Override
      public void unRegisterListener(String config, RuntimeConfigListener listener)
      {
         listeners.remove(config, listener);
         
      }
      @Override
      public RuntimeConfigListener getListeners(String config)
      {
         return listeners.get(config);
      }
          
  }
