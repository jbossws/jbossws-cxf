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
package org.jboss.test.ws.jaxws.jbws1694;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1694TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1694.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Basket.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.BasketEntries.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Header.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694EndpointSEI.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694TestCase.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Receipt.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testInheritanceRpc() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jbws1694/JBWS1694Endpoint?wsdl");
      QName serviceName = new QName("http://jbws1694.jaxws.ws.test.jboss.org/", "JBWS1694EndpointService");
      Service service = Service.create(wsdlURL, serviceName);

      JBWS1694EndpointSEI port = service.getPort(JBWS1694EndpointSEI .class);

      Header inout = new Header();
      inout.setUuid("1234");

      Basket basket = new Basket();
      basket.setCustomerId("4567");

      Receipt receipt = port.submitBasket(new Holder<Header>(inout), basket);
      assertTrue(receipt.getMsg().equals("1234"));
   }
}
