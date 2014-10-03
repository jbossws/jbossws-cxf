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
package org.jboss.test.ws.jaxws.jbws3282;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.helper.ClientHelper;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;

public class Helper implements ClientHelper
{
   private final String targetNS = "http://jbws3282.jaxws.ws.test.jboss.org/";
   private final String testConfig = "org.jboss.test.ws.jaxws.jbws3282.Endpoint2Impl";
   private String address;
   private static volatile EndpointConfig defaultEndpointConfig;
   
   public boolean testHandlerChainVanillaServer() throws Exception
   {
      QName serviceName = new QName(targetNS, "Endpoint2ImplService");
      URL wsdlURL = new URL(address + "/ep2?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      String resStr = port.echo("Kermit");
      if (!"Kermit|EpIn|endpoint2|EpOut".equals(resStr)) {
         return false;
      }
      
      serviceName = new QName(targetNS, "Endpoint3ImplService");
      wsdlURL = new URL(address + "/ep3?wsdl");
      service = Service.create(wsdlURL, serviceName);
      port = (Endpoint)service.getPort(Endpoint.class);
      resStr = port.echo("Kermit");
      if (!"Kermit|EpIn|endpoint3|EpOut".equals(resStr)) {
         return false;
      }
      
      return true;
   }
   
   public boolean setupConfigurations() throws Exception
   {
      defaultEndpointConfig = TestUtils.getAndVerifyDefaultEndpointConfiguration();
      TestUtils.addTestCaseEndpointConfiguration(testConfig);
      TestUtils.changeDefaultEndpointConfiguration();
      return true;
   }
   
   public boolean restoreConfigurations() throws Exception
   {
      TestUtils.setEndpointConfigAndReload(defaultEndpointConfig);
      TestUtils.removeTestCaseEndpointConfiguration(testConfig);
      defaultEndpointConfig = null;
      return true;
   }

   public boolean testHandlerChainModifiedServer() throws Exception
   {
      final EndpointConfig defaultEndpointConfig = TestUtils.getAndVerifyDefaultEndpointConfiguration();
      final String testConfig = "org.jboss.test.ws.jaxws.jbws3282.Endpoint2Impl";
      try {
         TestUtils.addTestCaseEndpointConfiguration(testConfig);
         TestUtils.changeDefaultEndpointConfiguration();
         
         QName serviceName = new QName(targetNS, "Endpoint2ImplService");
         URL wsdlURL = new URL(address + "/ep2?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         String resStr = port.echo("Kermit");
         if (!"Kermit|RoutIn|EpIn|endpoint2|EpOut|RoutOut".equals(resStr)) {
            return false;
         }
         
         serviceName = new QName(targetNS, "Endpoint3ImplService");
         wsdlURL = new URL(address + "/ep3?wsdl");
         service = Service.create(wsdlURL, serviceName);
         port = (Endpoint)service.getPort(Endpoint.class);
         resStr = port.echo("Kermit");
         if (!"Kermit|EpIn|LogIn|endpoint3|LogOut|EpOut".equals(resStr)) {
            return false;
         }
         
         return true;
      } finally {
         TestUtils.setEndpointConfigAndReload(defaultEndpointConfig);
         TestUtils.removeTestCaseEndpointConfiguration(testConfig);
      }
   }
   
   @Override
   public void setTargetEndpoint(String address)
   {
      this.address = address;
   }
}
