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

import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.message.Message;
import org.jboss.ws.common.invocation.RecordingServerHandler;
import org.jboss.wsf.spi.deployment.RuntimeConfig;
/**
 * Config listener to add RecodingServerHandler when record-enabled config changes
 * @author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
@NoJSR250Annotations(unlessNull = "bus")
public class RecordConfigListener implements RuntimeConfigListener
{
   private String propertyName;
   private Class<? extends InterceptorProvider> claz;
   private RuntimeConfigListenerManager configListenManager;
   private RecordingServerHandler newRecordingServerHandler;

   public RecordConfigListener()
   {
      this(RuntimeConfig.RECORD_ENABLED, Endpoint.class);
   }

   public RecordConfigListener(String propertyName, Class<? extends InterceptorProvider> target)
   {
      this.propertyName = propertyName;
      this.claz = target;
   }

   @Resource
   public final void setBus(Bus bus)
   {
      if (null != bus)
      {
         configListenManager = bus.getExtension(RuntimeConfigListenerManager.class);
         if (null != configListenManager)
         {
            configListenManager.registerListener(propertyName, this);
         }
      }
   }

   public void configChange(Message message, String configValue)
   {
      if ("false".equals(configValue))
      {
         onDisable(message);
      }
      if ("true".equals(configValue))
      {
         onEnable(message);
      }

   }

   @SuppressWarnings("rawtypes")
   public void onEnable(Message message)
   {
      org.jboss.wsf.spi.deployment.Endpoint endpoint = message.getExchange().get(org.jboss.wsf.spi.deployment.Endpoint.class);
      endpoint.setRuntimeProperty(RuntimeConfig.PROCESSOR, "MemoryBufferRecorder");
      boolean handlerAdded = false;
      if (message.getExchange().getEndpoint() instanceof JaxWsEndpointImpl)
      {
         //RecordingServerHandler handler = new RecordingServerHandler(); 

         List<Handler> chain = ((JaxWsEndpointImpl)message.getExchange().getEndpoint()).getJaxwsBinding().getHandlerChain();
         for (Handler handler : chain)
         {
            if (handler instanceof RecordingServerHandler)
            {
               handlerAdded = true;
               break;
            }

         }
         if (!handlerAdded)
         {
            newRecordingServerHandler = new RecordingServerHandler();
            chain.add(newRecordingServerHandler);
            ((JaxWsEndpointImpl)message.getExchange().getEndpoint()).getJaxwsBinding().setHandlerChain(chain);
         }
      }
   }

   @SuppressWarnings("rawtypes")
   public void onDisable(Message message)
   {
      org.jboss.wsf.spi.deployment.Endpoint endpoint = message.getExchange().get(org.jboss.wsf.spi.deployment.Endpoint.class);
      endpoint.removeProperty(RuntimeConfig.PROCESSOR);
      if (message.getExchange().getEndpoint() instanceof JaxWsEndpointImpl)
      {
         List<Handler> chain = ((JaxWsEndpointImpl)message.getExchange().getEndpoint()).getJaxwsBinding().getHandlerChain();
         chain.remove(newRecordingServerHandler);
         ((JaxWsEndpointImpl)message.getExchange().getEndpoint()).getJaxwsBinding().setHandlerChain(chain);
      }
   }

   public String getConfigName()
   {
      return propertyName;

   }

   @Override
   public Class<? extends InterceptorProvider> getTarget()
   {
      return claz;
   }

}
