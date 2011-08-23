/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.jboss.logging.Logger;
import org.jboss.wsf.test.ClientHelper;

public class UsernameHelper implements ClientHelper
{
   private String endpointURL;
   
   public UsernameHelper()
   {
      
   }

   public UsernameHelper(String endpointURL)
   {
      setTargetEndpoint(endpointURL);
   }

   public boolean test() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
         URL wsdlURL = new URL(endpointURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
         setupWsse(proxy, "kermit");

         return ("Secure Hello World!".equals(proxy.sayHello()));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public boolean testWrongPassword() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
         URL wsdlURL = new URL(endpointURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         setupWsse(proxy, "snoopy");
         try
         {
            proxy.sayHello();
            Logger.getLogger(this.getClass()).error("User snoopy shouldn't be authenticated.");
            return false;
         }
         catch (Exception e)
         {
            return true;
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   
   private void setupWsse(ServiceIface proxy, String username)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      
      Map<String,Object> outProps = new HashMap<String,Object>();
      outProps.put("action", "UsernameToken");
      outProps.put("user", username);
      outProps.put("passwordType", "PasswordText");
      outProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.UsernamePasswordCallback");
      WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps); //request
      cxfEndpoint.getOutInterceptors().add(wssOut);
      cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.endpointURL = address;
   }
}
