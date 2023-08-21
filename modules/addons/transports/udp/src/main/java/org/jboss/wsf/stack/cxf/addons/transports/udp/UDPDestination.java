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
package org.jboss.wsf.stack.cxf.addons.transports.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.URI;
import org.jboss.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.LoadingByteArrayOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.workqueue.AutomaticWorkQueue;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

/**
 * A modified version of Apache CXF org.apache.cxf.transport.udp.UDPDestination
 * that does not rely on Apache Mina and directly uses basic java.io for any
 * type of datagram.
 *
 * @author alessio.soldano@jboss.com
 */
public class UDPDestination extends AbstractDestination
{
   private static final Logger LOG = Logger.getLogger(UDPDestination.class);
   private static final java.util.logging.Logger JAVA_LOG = LogUtils.getL7dLogger(UDPDestination.class);

   AutomaticWorkQueue queue;

   volatile DatagramSocket socket;

   public UDPDestination(Bus b, EndpointReferenceType ref, EndpointInfo ei)
   {
      super(b, ref, ei);
   }

   class SocketListener implements Runnable
   {
      public void run()
      {
         while (true)
         {
            if (socket == null)
            {
               return;
            }
            try
            {
               byte bytes[] = new byte[64 * 1024];
               final DatagramPacket p = new DatagramPacket(bytes, bytes.length);
               socket.receive(p);

               LoadingByteArrayOutputStream out = new LoadingByteArrayOutputStream()
               {
                  public void close() throws IOException
                  {
                     super.close();
                     final DatagramPacket p2 = new DatagramPacket(getRawBytes(), 0, this.size(), p.getSocketAddress());
                     socket.send(p2);
                  }
               };

               UDPConnectionInfo info = new UDPConnectionInfo(out, new ByteArrayInputStream(bytes, 0, p.getLength()));

               final MessageImpl m = new MessageImpl();
               final Exchange exchange = new ExchangeImpl();
               exchange.setDestination(UDPDestination.this);
               m.setDestination(UDPDestination.this);
               exchange.setInMessage(m);
               m.setContent(InputStream.class, info.in);
               m.put(UDPConnectionInfo.class, info);
               queue.execute(new Runnable()
               {
                  public void run()
                  {
                     getMessageObserver().onMessage(m);
                  }
               });
            }
            catch (IOException e)
            {
               if (socket != null) {
                  LOG.error(e.toString());
               }
            }
         }
      }
   }

   /** {@inheritDoc}*/
   @Override
   protected Conduit getInbuiltBackChannel(Message inMessage)
   {
      if (inMessage.getExchange().isOneWay())
      {
         return null;
      }
      final UDPConnectionInfo info = inMessage.get(UDPConnectionInfo.class);
      return new AbstractBackChannelConduit()
      {
         public void prepare(Message message) throws IOException
         {
            message.setContent(OutputStream.class, info.out);
         }
      };
   }

   /** {@inheritDoc}*/
   @Override
   protected java.util.logging.Logger getLogger()
   {
      return JAVA_LOG;
   }

   protected void activate()
   {
      WorkQueueManager queuem = bus.getExtension(WorkQueueManager.class);
      queue = queuem.getNamedWorkQueue("udp-transport");
      if (queue == null)
      {
         queue = queuem.getAutomaticWorkQueue();
      }

      try
      {
         URI uri = new URI(this.getAddress().getAddress().getValue());
         InetSocketAddress isa = null;
         if (StringUtils.isEmpty(uri.getHost()))
         {
            String s = uri.getSchemeSpecificPart();
            if (s.startsWith("//:"))
            {
               s = s.substring(3);
            }
            if (s.indexOf('/') != -1)
            {
               s = s.substring(0, s.indexOf('/'));
            }
            int port = Integer.parseInt(s);
            isa = new InetSocketAddress(port);
         }
         else
         {
            isa = new InetSocketAddress(uri.getHost(), uri.getPort());
         }
         DatagramSocket s;
         if (isa.getAddress().isMulticastAddress())
         {
            s = new MulticastSocket(null);
            ((MulticastSocket) s).setTimeToLive(1);
            s.bind(new InetSocketAddress(isa.getPort()));
            ((MulticastSocket) s).joinGroup(isa.getAddress());
         }
         else
         {
            s = new DatagramSocket(null);
            s.bind(new InetSocketAddress(isa.getAddress(), isa.getPort()));
         }
         s.setReuseAddress(true);
         s.setReceiveBufferSize(64 * 1024);
         s.setSendBufferSize(64 * 1024);
         socket = s;
         queue.execute(new SocketListener());
      }
      catch (Exception ex)
      {
         LOG.error(ex.toString());
         throw new RuntimeException(ex);
      }
   }

   protected void deactivate()
   {
      if (socket != null)
      {
         DatagramSocket s = socket;
         socket = null;
         s.close();
      }
   }

   static class UDPConnectionInfo
   {
      final OutputStream out;

      final InputStream in;

      public UDPConnectionInfo(OutputStream o, InputStream i)
      {
         out = o;
         in = i;
      }
   }
}
