/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test case for the CXFClientConfigurer
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Oct-2014
 * 
 */
public class CXFClientConfigurerTest
{
   @Test
   public void testSetMapOfProperties() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         
         Service service = Service.create(this.getClass().getResource("META-INF/TestService.wsdl"), new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService"));
         EndpointInterface port = service.getPort(new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointInterfacePort"), EndpointInterface.class);
         Client client = ClientProxy.getClient(port);
         
         CXFClientConfigurer cfg = new CXFClientConfigurer();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("A", "1");
         properties.put("B", "2");
         properties.put("C", "3");
         properties.put("D", "4");
         properties.put("E", "5");
         
         cfg.setConfigProperties(client, properties);
         
         assertEquals("1", client.getEndpoint().get("A"));
         assertEquals("2", client.getEndpoint().get("B"));
         assertEquals("3", client.getEndpoint().get("C"));
         assertEquals("4", client.getEndpoint().get("D"));
         assertEquals("5", client.getEndpoint().get("E"));
         assertEquals(5, client.getEndpoint().size());
         
         properties = new HashMap<String, String>();
         properties.put("E", "10");
         properties.put("F", "20");
         properties.put("G", "30");
         
         cfg.setConfigProperties(client, properties);
         
         assertEquals("10", client.getEndpoint().get("E"));
         assertEquals("20", client.getEndpoint().get("F"));
         assertEquals("30", client.getEndpoint().get("G"));
         assertEquals(7, client.getEndpoint().size());
      } finally {
         if (bus != null) {
            bus.shutdown(true);
         }
      }
   }

   @Test
   public void testSetConfigProperties() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         
         Service service = Service.create(this.getClass().getResource("META-INF/TestService.wsdl"), new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService"));
         EndpointInterface port = service.getPort(new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointInterfacePort"), EndpointInterface.class);
         Client client = ClientProxy.getClient(port);
         
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("A", "1");
         properties.put("B", "2");
         properties.put("C", "3");
         properties.put("D", "4");
         properties.put("E", "5");
         ClientConfig clientConfig = new ClientConfig("Foo", null, null, properties, null);
         
         CXFClientConfigurer cfg = new CXFClientConfigurer();
         
         cfg.setConfigProperties(port, clientConfig);
         
         assertEquals("1", client.getEndpoint().get("A"));
         assertEquals("2", client.getEndpoint().get("B"));
         assertEquals("3", client.getEndpoint().get("C"));
         assertEquals("4", client.getEndpoint().get("D"));
         assertEquals("5", client.getEndpoint().get("E"));
         
         
         properties = new HashMap<String, String>();
         properties.put("E", "10");
         properties.put("F", "20");
         properties.put("G", "30");
         clientConfig = new ClientConfig("Foo2", null, null, properties, null);
         
         cfg.setConfigProperties(port, clientConfig);
         
         assertEquals(null, client.getEndpoint().get("A"));
         assertEquals(null, client.getEndpoint().get("B"));
         assertEquals(null, client.getEndpoint().get("C"));
         assertEquals(null, client.getEndpoint().get("D"));
         assertEquals("10", client.getEndpoint().get("E"));
         assertEquals("20", client.getEndpoint().get("F"));
         assertEquals("30", client.getEndpoint().get("G"));
      } finally {
         if (bus != null) {
            bus.shutdown(true);
         }
      }
   }

   @Test
   public void testAddInterceptors() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         
         Service service = Service.create(this.getClass().getResource("META-INF/TestService.wsdl"), new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService"));
         EndpointInterface port = service.getPort(new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointInterfacePort"), EndpointInterface.class);
         Client client = ClientProxy.getClient(port);
         
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC,org.jboss.wsf.stack.cxf.client.configuration.InterceptorD");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorE,org.jboss.wsf.stack.cxf.client.configuration.InterceptorF");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorG org.jboss.wsf.stack.cxf.client.configuration.InterceptorH");
         
         InterceptorUtils.addInterceptors(client, properties);
         
         List<String> interceptors = toNameList(client.getInInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         
         
         properties = new HashMap<String, String>();
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorD, FooInterceptor");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA, BarInterceptor");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC");
         
         InterceptorUtils.addInterceptors(client, properties);
         
         interceptors = toNameList(client.getInInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertFalse(interceptors.contains("FooInterceptor"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertFalse(interceptors.contains("BarInterceptor"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
      } finally {
         if (bus != null) {
            bus.shutdown(true);
         }
      }
   }

   @Test
   public void testAddInterceptorsThroughSetMapOfProperties() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         
         Service service = Service.create(this.getClass().getResource("META-INF/TestService.wsdl"), new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService"));
         EndpointInterface port = service.getPort(new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointInterfacePort"), EndpointInterface.class);
         Client client = ClientProxy.getClient(port);
         
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("A", "1");
         properties.put("B", "2");
         properties.put("C", "3");
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC,org.jboss.wsf.stack.cxf.client.configuration.InterceptorD");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorE,org.jboss.wsf.stack.cxf.client.configuration.InterceptorF");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorG org.jboss.wsf.stack.cxf.client.configuration.InterceptorH");
         
         CXFClientConfigurer cfg = new CXFClientConfigurer();
         
         cfg.setConfigProperties(client, properties);
         
         assertEquals("1", client.getEndpoint().get("A"));
         assertEquals("2", client.getEndpoint().get("B"));
         assertEquals("3", client.getEndpoint().get("C"));
         List<String> interceptors = toNameList(client.getInInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         
         
         properties = new HashMap<String, String>();
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorD, FooInterceptor");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA, BarInterceptor");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC");
         
         InterceptorUtils.addInterceptors(client, properties);
         
         assertEquals("1", client.getEndpoint().get("A"));
         assertEquals("2", client.getEndpoint().get("B"));
         assertEquals("3", client.getEndpoint().get("C"));
         interceptors = toNameList(client.getInInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertFalse(interceptors.contains("FooInterceptor"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertFalse(interceptors.contains("BarInterceptor"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
      } finally {
         if (bus != null) {
            bus.shutdown(true);
         }
      }
   }

   @Test
   public void testAddInterceptorsThroughSetConfigProperties() throws Exception
   {
      Bus bus = null;
      try {
         bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         
         Service service = Service.create(this.getClass().getResource("META-INF/TestService.wsdl"), new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService"));
         EndpointInterface port = service.getPort(new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointInterfacePort"), EndpointInterface.class);
         Client client = ClientProxy.getClient(port);
         
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("A", "1");
         properties.put("B", "2");
         properties.put("C", "3");
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC,org.jboss.wsf.stack.cxf.client.configuration.InterceptorD");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorE,org.jboss.wsf.stack.cxf.client.configuration.InterceptorF");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorG org.jboss.wsf.stack.cxf.client.configuration.InterceptorH");
         ClientConfig clientConfig = new ClientConfig("Foo", null, null, properties, null);
         
         CXFClientConfigurer cfg = new CXFClientConfigurer();
         
         cfg.setConfigProperties(port, clientConfig);
         
         assertEquals("1", client.getEndpoint().get("A"));
         assertEquals("2", client.getEndpoint().get("B"));
         assertEquals("3", client.getEndpoint().get("C"));
         List<String> interceptors = toNameList(client.getInInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         
         
         ClientProxy.getClient(port).getInInterceptors().add(new InterceptorZ());
         ClientProxy.getClient(port).getInFaultInterceptors().add(new InterceptorY());
         
         properties = new HashMap<String, String>();
         properties.put("E", "10");
         properties.put("F", "20");
         properties.put("G", "30");
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorD, FooInterceptor");
         properties.put(Constants.CXF_OUT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorB");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorA, BarInterceptor");
         properties.put(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP, "org.jboss.wsf.stack.cxf.client.configuration.InterceptorC");
         clientConfig = new ClientConfig("Foo2", null, null, properties, null);
         
         cfg.setConfigProperties(port, clientConfig);
         
         assertEquals(null, client.getEndpoint().get("A"));
         assertEquals(null, client.getEndpoint().get("B"));
         assertEquals(null, client.getEndpoint().get("C"));
         assertEquals(null, client.getEndpoint().get("D"));
         assertEquals("10", client.getEndpoint().get("E"));
         assertEquals("20", client.getEndpoint().get("F"));
         assertEquals("30", client.getEndpoint().get("G"));
         interceptors = toNameList(client.getInInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorZ"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorY"));
         assertFalse(interceptors.contains("BarInterceptor"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));

         properties = new HashMap<String, String>();
         properties.put(Constants.CXF_IN_INTERCEPTORS_PROP, "");
         properties.put(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP, "");
         clientConfig = new ClientConfig("Foo2", null, null, properties, null);
         
         cfg.setConfigProperties(port, clientConfig);
         
         assertEquals(null, client.getEndpoint().get("A"));
         assertEquals(null, client.getEndpoint().get("B"));
         assertEquals(null, client.getEndpoint().get("C"));
         assertEquals(null, client.getEndpoint().get("D"));
         assertEquals(null, client.getEndpoint().get("E"));
         assertEquals(null, client.getEndpoint().get("F"));
         assertEquals(null, client.getEndpoint().get("G"));
         interceptors = toNameList(client.getInInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorZ"));
         interceptors = toNameList(client.getOutInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         interceptors = toNameList(client.getInFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
         assertTrue(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorY"));
         assertFalse(interceptors.contains("BarInterceptor"));
         interceptors = toNameList(client.getOutFaultInterceptors());
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorA"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorB"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorC"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorD"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorE"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorF"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorG"));
         assertFalse(interceptors.contains("org.jboss.wsf.stack.cxf.client.configuration.InterceptorH"));
      } finally {
         if (bus != null) {
            bus.shutdown(true);
         }
      }
   }

   private static List<String> toNameList(Collection<Interceptor<?>> interceptors) {
      List<String> list = new ArrayList<String>();
      for (Interceptor<?> interceptor : interceptors) {
         list.add(interceptor.getClass().getName());
      }
      return list;
   }
}
