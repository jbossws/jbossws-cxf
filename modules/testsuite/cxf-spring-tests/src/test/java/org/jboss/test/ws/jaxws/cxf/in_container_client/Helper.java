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
package org.jboss.test.ws.jaxws.cxf.in_container_client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.logging.Logger;
import org.jboss.wsf.stack.cxf.client.ClientBusSelector;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.test.ClientHelper;

public class Helper implements ClientHelper
{
   private String targetEndpointURL;
   private Logger log = Logger.getLogger(Helper.class);
   
   public Helper()
   {
      
   }

   public Helper(String endpointURL)
   {
      setTargetEndpoint(endpointURL);
   }

   public boolean test() throws Exception
   {
      BusFactory factory = BusFactory.newInstance();
      if (!(factory instanceof JBossWSBusFactory)) { //check jbossws-cxf integration is on
         log.error("Expected instance of " + JBossWSBusFactory.class + " but got: " + factory.getClass());
         return false;
      }
      Bus bus = ((JBossWSBusFactory)factory).createBus("cxf.xml"); //force Spring bus construction、
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         HelloWorld port = getPort();
         return ("foo".equals(port.echo("foo")));
      }
      finally
      {
         bus.shutdown(true);
      }
      
   }
   
   public boolean testSpringBus() throws Exception
   {
      BusFactory factory = BusFactory.newInstance();
      if (!(factory instanceof JBossWSBusFactory)) { //check jbossws-cxf integration is on
         log.error("Expected instance of " + JBossWSBusFactory.class + " but got: " + factory.getClass());
         return false;
      }
      Bus bus = ClientBusSelector.getInstance().createNewBus(); //force Spring bus construction
      assert bus.getOutInterceptors().isEmpty() == false; // cxf-client.xml bus has at least one outinterceptor
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         HelloWorld port = getPort();
         return ("foo".equals(port.echo("foo")));
      }
      finally
      {
         bus.shutdown(true);
      }
      
   }
   
   private HelloWorld getPort() throws MalformedURLException
   {
      URL wsdlURL = new URL(targetEndpointURL + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/in_container_client", "HelloWorldService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/in_container_client", "HelloWorldImplPort");
      return (HelloWorld) service.getPort(portQName, HelloWorld.class);
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.targetEndpointURL = address;
   }
}
