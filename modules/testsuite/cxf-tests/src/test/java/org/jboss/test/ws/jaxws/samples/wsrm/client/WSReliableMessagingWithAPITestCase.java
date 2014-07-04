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

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.test.ws.jaxws.samples.wsrm.generated.SimpleService;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

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
   
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsrm-api.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.RMCheckInterceptor.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.SimpleServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Echo.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.EchoResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Ping.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl"), "wsdl/SimpleService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WSReliableMessagingWithAPITestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
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
         
         assertEquals("Hello World!", proxy.echo("Hello World!")); // request response call
         proxy.ping(); // one way call
      } finally {
         bus.shutdown(true);
      }
   }
   
}
