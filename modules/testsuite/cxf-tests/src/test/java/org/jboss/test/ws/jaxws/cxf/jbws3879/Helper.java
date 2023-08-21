/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3879;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.ClientHelper;

public class Helper implements ClientHelper
{
   private String gzipFeatureEndpointURL;
   
   public Helper()
   {
      
   }

   public Helper(String endpointURL)
   {
      setTargetEndpoint(endpointURL);
   }

   public boolean testGZIPUsingFeatureOnClient() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         HelloWorld port = getPort();
         
         ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
         configurer.setConfigProperties(port, "jaxws-client-config.xml", "Feature Client Config");
         
         return "foo".equals(port.echo("foo"));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public boolean testGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         HelloWorld port = getPort();
         Client client = ClientProxy.getClient(port);
         HTTPConduit conduit = (HTTPConduit)client.getConduit();
         HTTPClientPolicy policy = conduit.getClient();
         //enable Accept gzip, otherwise the server will not try to reply using gzip
         policy.setAcceptEncoding("gzip;q=1.0, identity; q=0.5, *;q=0");
         //add interceptor for decoding gzip message
         
         ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
         configurer.setConfigProperties(port, "jaxws-client-config.xml", "Interceptor Client Config");
         
         return ("foo".equals(port.echo("foo")));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public boolean testFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy policy = conduit.getClient();
      //enable Accept gzip, otherwise the server will not try to reply using gzip
      policy.setAcceptEncoding("gzip;q=1.0, identity; q=0.5, *;q=0");
      try
      {
         port.echo("foo");
         return false;
      }
      catch (Exception e)
      {
         //expected exception, as the client is not able to decode gzip message
         return true;
      }
   }
   
   private HelloWorld getPort(WebServiceFeature... features) throws MalformedURLException
   {
      URL wsdlURL = new URL(gzipFeatureEndpointURL + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jbws3879", "HelloWorldService");
      Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/jbws3879", "HelloWorldImplPort");
      return (HelloWorld) service.getPort(portQName, HelloWorld.class, features);
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.gzipFeatureEndpointURL = address;
   }
}
