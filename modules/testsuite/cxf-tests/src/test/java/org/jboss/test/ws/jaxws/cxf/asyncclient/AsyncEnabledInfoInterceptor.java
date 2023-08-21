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
