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
package org.jboss.test.ws.jaxrpc.samples.docstyle.bare;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JSE test case for an document/literal/bare style service.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Jan-2005
 */
public class TrivialServiceDocBareTestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/docstyle/bare";
   private static final String TARGET_ENDPOINT_URL = "http://" + getServerHost() + ":8080/jaxrpc-samples-docstyle-bare";
   private static TrivialService port;

   public static Test suite()
   {
      return new JBossWSTestSetup(TrivialServiceDocBareTestCase.class, "jaxrpc-samples-docstyle-bare.war, jaxrpc-samples-docstyle-bare-client.jar", new CleanupOperation() {
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
         port = getService(TrivialService.class, "SampleService", "TrivialServicePort");
      }
   }

   @SuppressWarnings("unchecked")
   protected <T> T getService(final Class<T> clazz, final String serviceName, final String portName) throws Exception {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_URL + "?wsdl"), new QName(TARGET_NAMESPACE, serviceName));
      return (T) service.getPort(new QName(TARGET_NAMESPACE, portName), clazz);
   }

   public void testProducPurchase() throws Exception
   {
      String person = "Kermit";
      String product = "Ferrari";
      TrivialOrder req = new TrivialOrder(person, product);
      TrivialOrderResponse res = port.purchase(req);
      assertEquals("ok" + person + product, res.getResult());
   }
}
