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
package org.jboss.test.ws.jaxws.samples.addressing;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test stateful endpoint using ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 24-Nov-2005
 */
public class AddressingStatefulTestCase extends JBossWSTest
{
   private static AddressingPort port1;
   private static AddressingPort port2;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(AddressingStatefulTestCase.class, "jaxws-samples-wsaddressing.war", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port1 = null;
            port2 = null;
         }
      });
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port1 == null || port2 == null)
      {
         URL wsdlURL = new URL(" http://" + getServerHost() + ":8080/jaxws-samples-wsaddressing/TestService?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/samples/wsaddressing", "TestService");

         Service service1 = Service.create(wsdlURL, serviceName);
         port1 = new AddressingPort(service1.getPort(StatefulEndpoint.class, new AddressingFeature(true, true)));

         Service service2 = Service.create(wsdlURL, serviceName);
         port2 = new AddressingPort(service2.getPort(StatefulEndpoint.class, new AddressingFeature(true, true)));
      }
   }

   public void testItemLifecycle() throws Exception
   {
      firstAddItem();
      secondGetItems();
      thirdCheckout();
   }

   private void firstAddItem() throws Exception
   {
      port1.addItem("Ice Cream");
      port1.addItem("Ferrari");

      port2.addItem("Mars Bar");
      port2.addItem("Porsche");
   }

   private void secondGetItems() throws Exception
   {
      String items1 = port1.getItems();
      assertEquals("[Ice Cream, Ferrari]", items1);

      String items2 = port2.getItems();
      assertEquals("[Mars Bar, Porsche]", items2);
   }

   private void thirdCheckout() throws Exception
   {
      port1.checkout();
      assertEquals("[]", port1.getItems());

      port2.checkout();
      assertEquals("[]", port2.getItems());
   }
}
