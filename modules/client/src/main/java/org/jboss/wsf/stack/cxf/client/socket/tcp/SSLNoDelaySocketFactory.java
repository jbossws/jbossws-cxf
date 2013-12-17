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

package org.jboss.wsf.stack.cxf.client.socket.tcp;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A wrapper SocketFactory implementation that will call setTcpNoDelay(true) whenever createSocket() is called. This
 * is done as a feature for the JIRA issue: JBWS-3745.
 *
 * @author navssurtani
 */
public class SSLNoDelaySocketFactory extends SSLSocketFactory
{

   private final SSLSocketFactory targetFactory;

   public SSLNoDelaySocketFactory(SSLSocketFactory targetFactory)
   {
      this.targetFactory = targetFactory;
   }


   @Override
   public String[] getDefaultCipherSuites()
   {
      return targetFactory.getDefaultCipherSuites();
   }

   @Override
   public String[] getSupportedCipherSuites()
   {
      return targetFactory.getSupportedCipherSuites();
   }

   @Override
   public Socket createSocket() throws IOException {
      Socket toReturn = targetFactory.createSocket();
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   @Override
   public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException
   {
      Socket toReturn = targetFactory.createSocket(socket, s, i, b);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   @Override
   public Socket createSocket(String s, int i) throws IOException, UnknownHostException
   {
      Socket toReturn = targetFactory.createSocket(s, i);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   @Override
   public Socket createSocket(String s, int i, InetAddress inetAddress, int i2) throws IOException, UnknownHostException
   {
      Socket toReturn = targetFactory.createSocket(s, i, inetAddress, i2);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   @Override
   public Socket createSocket(InetAddress inetAddress, int i) throws IOException
   {
      Socket toReturn = targetFactory.createSocket(inetAddress, i);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }

   @Override
   public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException
   {
      Socket toReturn = targetFactory.createSocket(inetAddress, i, inetAddress2, i2);
      toReturn.setTcpNoDelay(true);
      return toReturn;
   }
}
