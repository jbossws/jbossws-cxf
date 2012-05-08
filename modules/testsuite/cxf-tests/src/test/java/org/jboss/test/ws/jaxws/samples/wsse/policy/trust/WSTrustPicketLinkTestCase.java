/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Trust test case using PicketLink implementation of STS
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Apr-2012
 */
public final class WSTrustPicketLinkTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust/SecurityService";
   private final String stsURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trustPicketLink-sts/PicketLinkSTS";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      return WSTrustTestUtils.getTestSetup(WSTrustPicketLinkTestCase.class,
            "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trustPicketLink-sts.war jaxws-samples-wsse-policy-trust.war");
   }

   public void test() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         final QName stsServiceName = new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTS");
         final QName stsPortName = new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTSPort");
         WSTrustTestUtils.setupWsse(proxy, bus, stsURL + "?wsdl", stsServiceName, stsPortName);
         
         assertEquals("WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }
}
