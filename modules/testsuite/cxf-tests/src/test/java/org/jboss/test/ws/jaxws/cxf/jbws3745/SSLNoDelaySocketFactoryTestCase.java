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

package org.jboss.test.ws.jaxws.cxf.jbws3745;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.jboss.wsf.stack.cxf.client.UseNewBusFeature;
import org.jboss.wsf.stack.cxf.client.socket.tcp.SSLNoDelaySocketFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Simple test case for {@link org.jboss.wsf.stack.cxf.client.socket.tcp.SSLNoDelaySocketFactory}
 * <p/>
 * We will get an {@link javax.net.ssl.SSLSocketFactory} from the client. This should be an instance of an
 * SSLNoDelaySocketFactory. Once we have this we will simply create the socket and check if the tcp no delay flag has
 * been enabled.
 *
 * @author navssurtani
 */
public class SSLNoDelaySocketFactoryTestCase extends JBossWSTest
{
   /* The SocketFactory instance that will be used to get hold of the socket to be tested */
   private SSLSocketFactory fromClient = null;

   private static final int SERVER_PORT = 8080;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3745.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3745.SimpleService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3745.SimpleServiceImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3745/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(SSLNoDelaySocketFactoryTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void setUp() throws Exception
   {
      // First we need the client parameters
      Client client = ClientProxy.getClient(getPort());
      HTTPConduit conduit = (HTTPConduit) client.getConduit();
      TLSClientParameters tls = conduit.getTlsClientParameters();

      // Now we need to build the socket factory
      SSLSocketFactory defaultSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLNoDelaySocketFactory noDelay = new SSLNoDelaySocketFactory(defaultSocketFactory);

      // Now set the socket factory and make a simple check.
      tls.setSSLSocketFactory(noDelay);
      fromClient = tls.getSSLSocketFactory();
      assertEquals(fromClient.getClass(), SSLNoDelaySocketFactory.class);
   }
   
   public void tearDown() throws Exception {
      fromClient = null;
   }

   public void testCreateSocket0() throws Exception
   {
      Socket toTest = null;
      try {
         toTest = fromClient.createSocket();
         assertTrue(toTest.getTcpNoDelay());
      } finally {
         if (toTest != null)
            toTest.close();
      }
   }

   public void testCreateSocket1() throws Exception
   {
      Socket toTest = null;
      try {
         toTest = fromClient.createSocket(getServerHost(), SERVER_PORT);
         assertTrue(toTest.getTcpNoDelay());
      } finally {
         if (toTest != null)
            toTest.close();
      }
   }

   public void testCreateSocket3() throws Exception
   {
      Socket toTest = null;
      try {
         toTest = fromClient.createSocket(InetAddress.getByName(getServerHost()), SERVER_PORT);
         assertTrue(toTest.getTcpNoDelay());
      } finally {
         if (toTest != null)
            toTest.close();
      }
   }

   private SimpleService getPort() throws Exception
   {
      final QName qname = new QName("http://org.jboss.ws/jaxws/cxf/jbws3745", "SimpleService");
      final Service service = Service.create(qname, new UseNewBusFeature());
      return service.getPort(SimpleService.class);
   }
}
