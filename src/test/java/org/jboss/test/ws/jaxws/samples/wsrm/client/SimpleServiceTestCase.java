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
package org.jboss.test.ws.jaxws.samples.wsrm.client;

import java.io.File;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.test.ws.jaxws.samples.wsrm.generated.SimpleService;

/**
 * Client invoking web service using WS-RM
 *
 * @author richard.opalka@jboss.com
 */
public final class SimpleServiceTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsrm/SimpleService";
   private SimpleService proxy;
   private Bus bus;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(SimpleServiceTestCase.class, "jaxws-samples-wsrm.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      SpringBusFactory busFactory = new SpringBusFactory();
      URL cxfConfig = new File("test-resources/jaxws/samples/wsrm/wsrm-client-config.xml").toURL();
      bus = busFactory.createBus(cxfConfig);
      busFactory.setDefaultBus(bus);

      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (SimpleService)service.getPort(SimpleService.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      bus.shutdown(true);

      super.tearDown();
   }
   
   public void test() throws Exception
   {
      proxy.ping(); // one way call
      assertEquals("Hello World!", proxy.echo("Hello World!")); // request responce call
   }
   
}
