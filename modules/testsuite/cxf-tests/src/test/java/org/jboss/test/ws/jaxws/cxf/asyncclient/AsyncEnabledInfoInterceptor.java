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
package org.jboss.test.ws.jaxws.cxf.asyncclient;

import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.slf4j.Slf4jVerboseEventSender;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class AsyncEnabledInfoInterceptor extends AbstractLoggingInterceptor
{
   private static final Logger LOG = LogUtils.getLogger(AsyncEnabledInfoInterceptor.class);
   private static boolean asyncEnabled = false;
   
   public AsyncEnabledInfoInterceptor () {
      super(Phase.PREPARE_SEND_ENDING, (LogEventSender)(new Slf4jVerboseEventSender()));
      this.addAfter(MessageSenderInterceptor.class.getName());
      this.addBefore(MessageSenderInterceptor.MessageSenderEndingInterceptor.class.getName());
   }
   
   public AsyncEnabledInfoInterceptor(String phase)
   {
      super(phase, (LogEventSender)(new Slf4jVerboseEventSender()));
   }
   public void handleMessage(Message message) throws Fault
   {
      if (message.get("use.async.http.conduit") == null) {
         asyncEnabled = false;
      } else {
         asyncEnabled = (Boolean)message.get("use.async.http.conduit");
      }
   }
   protected Logger getLogger()
   {
      return LOG;
   }
   
   public boolean isAsyncEnabled() {
      return asyncEnabled;
   }
}
