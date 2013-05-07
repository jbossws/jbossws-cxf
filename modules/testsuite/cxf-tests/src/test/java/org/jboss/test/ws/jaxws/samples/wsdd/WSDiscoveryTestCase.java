/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsdd;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.discovery.WSDiscoveryClient;
import org.apache.cxf.ws.discovery.wsdl.ProbeMatchType;
import org.apache.cxf.ws.discovery.wsdl.ProbeMatchesType;
import org.apache.cxf.ws.discovery.wsdl.ProbeType;
import org.apache.cxf.ws.discovery.wsdl.ResolveMatchType;
import org.apache.cxf.ws.discovery.wsdl.ScopesType;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Discovery 1.1 sample
 *
 * @author alessio.soldano@jboss.com
 * @since 07-May-2013
 */
public final class WSDiscoveryTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WSDiscoveryTestCase.class, "jaxws-samples-wsdd.war,jaxws-samples-wsdd2.war");
   }
   
   public void testProbeAndResolve() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         
         WSDiscoveryClient client = new WSDiscoveryClient(bus);
         ProbeType pt = new ProbeType();
         ScopesType scopes = new ScopesType();
         pt.setScopes(scopes);
         ProbeMatchesType pmts = client.probe(pt);
         assertNotNull(pmts);
         assertEquals(3, pmts.getProbeMatch().size());
         List<ResolveMatchType> rmts = new LinkedList<ResolveMatchType>();
         for (ProbeMatchType pmt : pmts.getProbeMatch()) {
            rmts.add(client.resolve(pmt.getEndpointReference()));
         }
         
         final QName typeName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsdd", "ServiceIface");
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd/WSDDService", typeName);
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd2/WSDDService", typeName);
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd2/AnotherWSDDService", typeName);
      } finally {
         bus.shutdown(true);
      }
   }
   
   public void testInvocation() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         
         WSDiscoveryClient client = new WSDiscoveryClient(bus);
         ProbeType pt = new ProbeType();
         ScopesType scopes = new ScopesType();
         pt.setScopes(scopes);
         ProbeMatchesType pmts = client.probe(pt);
         assertNotNull(pmts);
         assertEquals(3, pmts.getProbeMatch().size());
         List<ResolveMatchType> rmts = new LinkedList<ResolveMatchType>();
         for (ProbeMatchType pmt : pmts.getProbeMatch()) {
            rmts.add(client.resolve(pmt.getEndpointReference()));
         }
         
         int i = 0;
         for (ResolveMatchType rmt : rmts) {
            i++;
            ServiceIface port = rmt.getEndpointReference().getPort(ServiceIface.class);
            String address = rmt.getXAddrs().iterator().next();
            ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
            String expected = address.contains("AnotherWSDDService") ? "Hi " : "Greetings ";
            assertEquals(expected +  "Alice" + i, port.greet("Alice" + i));
         }
      } finally {
         bus.shutdown(true);
      }
   }
   
   private void checkResolveMatches(List<ResolveMatchType> rmts, String address, QName type) {
      List<ResolveMatchType> rmtList = getByAddress(rmts, address);
      assertEquals(1, rmtList.size());
      assertEquals(type, rmtList.get(0).getTypes().iterator().next());
   }
   
   private List<ResolveMatchType> getByAddress(List<ResolveMatchType> rmts, String address)
   {
      List<ResolveMatchType> list = new LinkedList<ResolveMatchType>();
      for (ResolveMatchType rmt : rmts)
      {
         for (String addr : rmt.getXAddrs())
         {
            if (address.equals(addr))
            {
               list.add(rmt);
            }
         }
      }
      return list;
   }
}
