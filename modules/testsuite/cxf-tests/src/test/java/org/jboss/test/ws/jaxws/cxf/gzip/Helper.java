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
package org.jboss.test.ws.jaxws.cxf.gzip;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.common.gzip.GZIPFeature;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
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

   public boolean testGZIPUsingFeatureOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         GZIPFeature gzipFeature = new GZIPFeature();
         gzipFeature.setThreshold(0);
         gzipFeature.initialize(bus);

         HelloWorld port = getPort();
         return ("foo".equals(port.echo("foo")));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public boolean testGZIPUsingFeatureOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      GZIPFeature gzipFeature = new GZIPFeature();
      gzipFeature.setThreshold(0);
      gzipFeature.initialize(client, BusFactory.getThreadDefaultBus());
      return ("foo".equals(port.echo("foo")));
   }
   
   public boolean testGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy policy = conduit.getClient();
      //enable Accept gzip, otherwise the server will not try to reply using gzip
      policy.setAcceptEncoding("gzip;q=1.0, identity; q=0.5, *;q=0");
      //add interceptor for decoding gzip message
      client.getInInterceptors().add(new GZIPEnforcingInInterceptor());
      return ("foo".equals(port.echo("foo")));
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
   
   public boolean testGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //enable Accept gzip, otherwise the server will not try to reply using gzip
         GZIPOutInterceptor outInterceptor = new GZIPOutInterceptor();
         outInterceptor.setThreshold(1024*1024); // 1MB -> no gzip on request
         bus.getOutInterceptors().add(outInterceptor);
         //add interceptor for decoding gzip message
         bus.getInInterceptors().add(new GZIPEnforcingInInterceptor());
         BusFactory.setThreadDefaultBus(bus);
         HelloWorld port = getPort();
         return ("foo".equals(port.echo("foo")));
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   public boolean testFailureGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //enable Accept gzip, otherwise the server will not try to reply using gzip
         GZIPOutInterceptor outInterceptor = new GZIPOutInterceptor();
         outInterceptor.setThreshold(1024*1024); // 1MB -> no gzip on request
         bus.getOutInterceptors().add(outInterceptor);
         BusFactory.setThreadDefaultBus(bus);
         HelloWorld port = getPort();
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
      finally
      {
         bus.shutdown(true);
      }
   }
   
   private HelloWorld getPort() throws MalformedURLException
   {
      URL wsdlURL = new URL(gzipFeatureEndpointURL + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/gzip", "HelloWorldService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/gzip", "HelloWorldImplPort");
      return (HelloWorld) service.getPort(portQName, HelloWorld.class);
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.gzipFeatureEndpointURL = address;
   }
}
