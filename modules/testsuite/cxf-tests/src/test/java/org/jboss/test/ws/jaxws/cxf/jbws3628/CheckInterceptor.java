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
