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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Enumeration;
import org.jboss.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.LoadingByteArrayOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

/**
 * A modified version of Apache CXF org.apache.cxf.transport.udp.UDPConduit
 * that does not rely on Apache Mina and directly uses basic java.io for
 * any type of datagram.
 *
 * @author alessio.soldano@jboss.com
 */
public class UDPConduit extends AbstractConduit
{
   private static final String MULTI_RESPONSE_TIMEOUT = "udp.multi.response.timeout";

   private static final Logger LOG = Logger.getLogger(UDPConduit.class);
   private static final java.util.logging.Logger JAVA_LOG = LogUtils.getL7dLogger(UDPConduit.class);

   Bus bus;

   public UDPConduit(EndpointReferenceType t, final Bus bus)
   {
      super(t);
      this.bus = bus;
   }

   private void dataReceived(Message message, byte bytes[], boolean async)
   {
      final Message inMessage = new MessageImpl();
      inMessage.setExchange(message.getExchange());
      message.getExchange().setInMessage(inMessage);
      inMessage.setContent(InputStream.class, new ByteArrayInputStream(bytes));
      incomingObserver.onMessage(inMessage);
      if (!message.getExchange().isSynchronous())
      {
         message.getExchange().setInMessage(null);
      }
   }

   public void prepare(final Message message) throws IOException
   {
      try
      {
         String address = (String) message.get(Message.ENDPOINT_ADDRESS);
         if (StringUtils.isEmpty(address))
         {
            address = this.getTarget().getAddress().getValue();
         }
         URI uri = new URI(address);
         if (StringUtils.isEmpty(uri.getHost())) { //broadcast
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
            send(message, null, port);
         } else {
            InetSocketAddress isa = new InetSocketAddress(uri.getHost(), uri.getPort());
            send(message, isa, isa.getPort());
         }
      }
      catch (Exception ex)
      {
         throw new IOException(ex);
      }
   }

   private void send(Message message, InetSocketAddress isa, int port)
   {
      message.setContent(OutputStream.class, new SocketOutputStream(port, isa, message));
   }

   private final class SocketOutputStream extends LoadingByteArrayOutputStream
   {
      private final int port;

      private final Message message;

      private final InetSocketAddress isa;

      private SocketOutputStream(int port, InetSocketAddress isa, Message message)
      {
         this.port = port;
         this.message = message;
         this.isa = isa;
      }

      public void close() throws IOException
      {
         super.close();
         DatagramSocket socket = (isa != null) ? new MulticastSocket(null) : new DatagramSocket();
         socket.setSendBufferSize(this.size());
         socket.setReceiveBufferSize(64 * 1024);
         socket.setBroadcast(true);

         if (isa == null) //broadcast
         {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
               NetworkInterface networkInterface = interfaces.nextElement();
               if (!networkInterface.isUp() || networkInterface.isLoopback())
               {
                  continue;
               }
               for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
               {
                  InetAddress broadcast = interfaceAddress.getBroadcast();
                  if (broadcast == null)
                  {
                     continue;
                  }
                  DatagramPacket sendPacket = new DatagramPacket(this.getRawBytes(), 0, this.size(), broadcast, port);
                  try
                  {
                     socket.send(sendPacket);
                  }
                  catch (Exception e)
                  {
                     //ignore
                     LOG.trace(e.getMessage(), e);
                  }
               }
            }
         }
         else
         {
            DatagramPacket sendPacket = new DatagramPacket(this.getRawBytes(), 0, this.size(), isa);
            try
            {
               socket.send(sendPacket);
            }
            catch (Exception e)
            {
               //ignore
               LOG.trace(e.getMessage(), e);
            }
         }

         if (!message.getExchange().isOneWay())
         {
            byte bytes[] = new byte[64 * 1024];
            DatagramPacket p = new DatagramPacket(bytes, bytes.length);
            Object to = message.getContextualProperty(MULTI_RESPONSE_TIMEOUT);
            Integer i = null;
            if (to instanceof String)
            {
               i = Integer.parseInt((String) to);
            }
            else if (to instanceof Integer)
            {
               i = (Integer) to;
            }
            if (i == null || i <= 0 || message.getExchange().isSynchronous())
            {
               socket.setSoTimeout(30000);
               socket.receive(p);
               dataReceived(message, bytes, false);
            }
            else
            {
               socket.setSoTimeout(i);
               boolean found = false;
               try
               {
                  while (true)
                  {
                     socket.receive(p);
                     dataReceived(message, bytes, false);
                     found = true;
                  }
               }
               catch (java.net.SocketTimeoutException ex)
               {
                  if (!found)
                  {
                     throw ex;
                  }
               }
            }
         }
         socket.close();
      }
   }

   @Override
   protected java.util.logging.Logger getLogger()
   {
      return JAVA_LOG;
   }

}
