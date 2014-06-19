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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * @author Heiko.Braun@jboss.com
 */
public class JBWS1694TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws1694.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Basket.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.BasketEntries.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Header.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694EndpointSEI.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694TestCase.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Receipt.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1694TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testInheritanceRpc() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jbws1694/JBWS1694Endpoint?wsdl");
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
