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
package org.jboss.test.ws.jaxws.cxf.gzip;

import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.common.gzip.GZIPInInterceptor;

public class GZIPEnforcingInInterceptor extends GZIPInInterceptor
{
   @Override
   public void handleMessage(Message message) throws Fault
   {
      Map<String, List<String>> protocolHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
      if (protocolHeaders != null)
      {
         List<String> contentEncoding = HttpHeaderHelper.getHeader(protocolHeaders,
               HttpHeaderHelper.CONTENT_ENCODING);
         if (contentEncoding != null && (contentEncoding.contains("gzip") || contentEncoding.contains("x-gzip")))
         {
            super.handleMessage(message);
            return;
         }
      }
      throw new RuntimeException("Content-Encoding gzip not found!");
   }
}
