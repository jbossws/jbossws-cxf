/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxrpc.samples.oneway;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JSE test case for onway operations.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 09-Sep-2005
 */
public class OneWayTestCase extends JBossWSTest
{
   private static final String TARGET_ENDPOINT_URL = "http://" + getServerHost() + ":8080/jaxrpc-samples-oneway";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/oneway";

   private static OneWayTestService port;

   public static Test suite()
   {
      return new JBossWSTestSetup(OneWayTestCase.class, "jaxrpc-samples-oneway.war, jaxrpc-samples-oneway-client.jar", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
         }
      });
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         port = getService(OneWayTestService.class, "TestService", "OneWayTestServicePort");
      }
   }

   @SuppressWarnings("unchecked")
   protected <T> T getService(final Class<T> clazz, final String serviceName, final String portName) throws Exception {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_URL + "?wsdl"), new QName(TARGET_NAMESPACE, serviceName));
      return (T) service.getPort(new QName(TARGET_NAMESPACE, portName), clazz);
   }

   public void testOneWay() throws Exception
   {
      port.oneWay("Hello");
   }

   public void testNoOneWay() throws Exception
   {
      port.noOneWay("Hello");
   }
}
