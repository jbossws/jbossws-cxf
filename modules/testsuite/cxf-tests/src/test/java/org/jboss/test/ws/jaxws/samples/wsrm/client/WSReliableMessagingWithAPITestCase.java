/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsrm.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.test.ws.jaxws.samples.wsrm.generated.SimpleService;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Client invoking web service with WS-RM and using no xml descriptor
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Aug-2010
 * 
 */
public final class WSReliableMessagingWithAPITestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsrm-api/SimpleService";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WSReliableMessagingWithAPITestCase.class, "jaxws-samples-wsrm-api.war");
   }

   public void test() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
         URL wsdlURL = getResourceURL("jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         SimpleService proxy = (SimpleService)service.getPort(SimpleService.class);
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
         
         proxy.ping(); // one way call
         assertEquals("Hello World!", proxy.echo("Hello World!")); // request response call
      } finally {
         bus.shutdown(true);
      }
   }
   
}
