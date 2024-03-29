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
package org.jboss.test.ws.jaxws.cxf.jbws3628;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * A server side interceptor that verifies every incoming message has WS-Addressing stuff
 */
public class CheckInterceptor extends AbstractPhaseInterceptor<Message>
{
   public CheckInterceptor()
   {
      super(Phase.RECEIVE);
   }
   
   public void handleMessage(Message message) throws Fault
   {
      String method = (String)message.get(Message.HTTP_REQUEST_METHOD);
      if (!method.equals("POST"))
      {
         return;
      }
      StringBuilder sb = new StringBuilder();
      InputStream is = message.getContent(InputStream.class);
      if (is != null)
      {
         CachedOutputStream bos = new CachedOutputStream();
         try
         {
            copy(is, bos, 4096);
            bos.flush();
            is.close();
            message.setContent(InputStream.class, bos.getInputStream());
            bos.writeCacheTo(sb);
            bos.close();
         }
         catch (IOException e)
         {
            throw new Fault(e);
         }
      }
      if (!sb.toString().contains("http://www.w3.org/2005/08/addressing"))
      {
         throw new Fault("Could not find any reference to namespace 'http://www.w3.org/2005/08/addressing' in handled message.",
               java.util.logging.Logger.getLogger(CheckInterceptor.class.getName()));
      }
   }

   public static int copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException
   {
      int avail = input.available();
      if (avail > 262144)
      {
         avail = 262144;
      }
      if (avail > bufferSize)
      {
         bufferSize = avail;
      }
      final byte[] buffer = new byte[bufferSize];
      int n = 0;
      n = input.read(buffer);
      int total = 0;
      while (-1 != n)
      {
         if (n == 0)
         {
            throw new IOException("0 bytes read in violation of InputStream.read(byte[])");
         }
         output.write(buffer, 0, n);
         total += n;
         n = input.read(buffer);
      }
      return total;
   }

}
