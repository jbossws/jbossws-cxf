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

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.discovery.WSDiscoveryClient;
import org.apache.cxf.ws.discovery.wsdl.ProbeMatchType;
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
   private static final int TIMEOUT = Integer.getInteger(WSDiscoveryTestCase.class.getName() + ".timeout", 2000);
   
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
         List<ProbeMatchType> pmts = filterProbeMatchesForHost(client.probe(pt, TIMEOUT).getProbeMatch(), getServerHost());
         
         List<ResolveMatchType> rmts = new LinkedList<ResolveMatchType>();
         for (ProbeMatchType pmt : pmts) {
            W3CEndpointReference epr = pmt.getEndpointReference();
            ResolveMatchType rmt = client.resolve(epr, TIMEOUT);
            assertNotNull("Could not resolve (timeout = " + TIMEOUT  + " ms) reference: " + epr, rmt);
            rmts.add(rmt);
         }
         
         final QName typeName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsdd", "ServiceIface");
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd/WSDDService", typeName);
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd2/WSDDService", typeName);
         checkResolveMatches(rmts, "http://" + getServerHost() + ":8080/jaxws-samples-wsdd2/AnotherWSDDService", typeName);
         client.close();
      } finally {
         bus.shutdown(true);
      }
   }
   
   private List<ProbeMatchType> filterProbeMatchesForHost(List<ProbeMatchType> probes, String serverHost)
   {
      final List<ProbeMatchType> filtered = new ArrayList<ProbeMatchType>();
      for (ProbeMatchType probeMatchType : probes)
      {
         final List<String> addresses = probeMatchType.getXAddrs();
         if (addresses == null || addresses.isEmpty()) {
            //add Probe Match if it has no address (which is optional and might be omitted on probe match result)
            filtered.add(probeMatchType);
         } else {
            for (String addr : addresses) {
               try {
                  final URL url = new URL(addr);
                  //add Probe Match only if it's from the current serverHost (for test purposes we do not want
                  //to consider match results that might be coming from other services on the same network)
                  if (url.getHost().contains(serverHost)) {
                     filtered.add(probeMatchType);
                     break;
                  }
               } catch (Exception e) {
                  //ignore and move on
               }
            }
         }
      }
      return filtered;
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
         List<ProbeMatchType> pmts = filterProbeMatchesForHost(client.probe(pt, TIMEOUT).getProbeMatch(), getServerHost());
         
         List<ResolveMatchType> rmts = new LinkedList<ResolveMatchType>();
         for (ProbeMatchType pmt : pmts) {
            W3CEndpointReference epr = pmt.getEndpointReference();
            ResolveMatchType rmt = client.resolve(epr, TIMEOUT);
            assertNotNull("Could not resolve (timeout = " + TIMEOUT  + " ms) reference: " + epr, rmt);
            rmts.add(rmt);
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
         client.close();
      } finally {
         bus.shutdown(true);
      }
   }
   
   private void checkResolveMatches(List<ResolveMatchType> rmts, String address, QName type) {
      List<ResolveMatchType> rmtList = getByAddress(rmts, address);

      //assertTrue("Number of matches can not be 0.", (rmtList.size() > 0));
      assertEquals(dbgDumpList(rmtList), 1, rmtList.size());
      assertEquals(type, rmtList.get(0).getTypes().iterator().next());
   }

   // tmp method for debugging jenkins runs.
   // report uuid of the endpoint
   private String dbgDumpList(List<ResolveMatchType> rmtList){
      StringBuilder dbgStr = new StringBuilder().append("\n");

      if(rmtList.size() > 1){
         for(ResolveMatchType rmt: rmtList){
            String tmpStr = rmt.getEndpointReference().toString();
            int start = tmpStr.indexOf("<Address>");
            int end = tmpStr.indexOf("</Address>");
            if (start > -1 && end > -1){
               String uuidStr = tmpStr.substring(start+9, end);
               dbgStr.append(rmt.getXAddrs().get(0) +"  " + uuidStr + "\n");
            }
         }
      }
      return dbgStr.toString();
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
