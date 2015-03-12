/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3879;

import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.common.gzip.GZIPInInterceptor;

public class GZIPEnforcingInInterceptor extends GZIPInInterceptor
{
   private String par = null;
   
   public String getPar()
   {
      return par;
   }

   public void setPar(String par)
   {
      this.par = par;
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      if (par == null) {
         throw new IllegalStateException();
      }
      
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
