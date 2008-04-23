/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.samples.wsa;

import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Client invoking web service using WS-Addressing
 *
 * @author richard.opalka@jboss.com
 */
public final class AddressingTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsa/AddressingService";
   private ServiceIface proxy;
   private Bus bus;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(AddressingTestCase.class, "jaxws-samples-wsa.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      SpringBusFactory busFactory = new SpringBusFactory();
      URL cxfConfig = new File("test-resources/jaxws/samples/wsa/cxf-client-config.xml").toURL();
      bus = busFactory.createBus(cxfConfig);
      busFactory.setDefaultBus(bus);

      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (ServiceIface)service.getPort(ServiceIface.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      bus.shutdown(true);

      super.tearDown();
   }
   
   public void test() throws Exception
   {
      assertEquals("Hello World!", proxy.sayHello());
   }
   
}
