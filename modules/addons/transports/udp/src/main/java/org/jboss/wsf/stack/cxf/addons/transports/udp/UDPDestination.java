/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
   private static final Logger LOG = LogUtils.getL7dLogger(UDPDestination.class);

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
                  LOG.log(Level.SEVERE, e.toString());
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
   protected Logger getLogger()
   {
      return LOG;
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
         LOG.log(Level.SEVERE, ex.toString());
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
