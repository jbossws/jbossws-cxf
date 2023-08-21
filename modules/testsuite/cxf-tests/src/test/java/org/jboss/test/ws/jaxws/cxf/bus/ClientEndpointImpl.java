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
