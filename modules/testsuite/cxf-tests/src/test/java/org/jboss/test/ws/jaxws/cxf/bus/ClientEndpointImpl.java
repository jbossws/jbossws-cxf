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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.net.URL;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.BusFactory;

@WebService(name = "ClientEndpoint", serviceName = "ClientEndpointService", targetNamespace = "http://org.jboss.ws/bus")
public class ClientEndpointImpl
{
   @WebMethod
   public String testClient(String input, String host) throws Exception
   {
      BusFactory.setThreadDefaultBus(null);
      Endpoint endpoint = getPort(getWsdlURL(host));
      return endpoint.echo(input);
   }

   @WebMethod
   public String testCachedPort(String input, String host) throws Exception
   {
      BusFactory.setThreadDefaultBus(null);
      Endpoint port = getPort(getWsdlURL(host));
      BusFactory.setThreadDefaultBus(null);
      return port.echo(input);
   }

   @WebMethod
   public String testCachedService(String input, String host) throws Exception
   {
      BusFactory.setThreadDefaultBus(null);
      Service service = getService(getWsdlURL(host));
      BusFactory.setThreadDefaultBus(null);
      Endpoint port = getPort(service);
      return port.echo(input);
   }

   private static URL getWsdlURL(String host) throws Exception
   {
      return new URL("http://" + host + "/jaxws-cxf-bus/EndpointService/Endpoint?wsdl");
   }

   private static Endpoint getPort(URL wsdlURL)
   {
      Service service = getService(wsdlURL);
      return getPort(service);
   }

   private static Endpoint getPort(Service service)
   {
      QName portQName = new QName("http://org.jboss.ws/bus", "EndpointPort");
      return (Endpoint) service.getPort(portQName, Endpoint.class);
   }

   private static Service getService(URL wsdlURL)
   {
      QName serviceName = new QName("http://org.jboss.ws/bus", "EndpointService");
      return Service.create(wsdlURL, serviceName);
   }
}
