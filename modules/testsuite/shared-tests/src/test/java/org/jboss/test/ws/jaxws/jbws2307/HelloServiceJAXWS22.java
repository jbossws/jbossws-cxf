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
package org.jboss.test.ws.jaxws.jbws2307;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "HelloService", targetNamespace = "http://helloservice.org/wsdl", wsdlLocation = "WEB-INF/wsdl/HelloService.wsdl")
public class HelloServiceJAXWS22 extends Service
{
   private static final URL HELLOSERVICE_WSDL_LOCATION;

   public HelloServiceJAXWS22(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public HelloServiceJAXWS22()
   {
      super(HELLOSERVICE_WSDL_LOCATION, new QName("http://helloservice.org/wsdl", "HelloService"));
   }
   
   public HelloServiceJAXWS22(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
      super(wsdlLocation, serviceName, features);
   }

   @WebEndpoint(name = "HelloPort")
   public Hello getHelloPort()
   {
      return ((Hello)super.getPort(new QName("http://helloservice.org/wsdl", "HelloPort"), Hello.class));
   }

   static
   {
      URL url = null;
      try
      {
         url = new URL("http://files1/releng/cts_5.x/cts-5.0c-temp/bin/WEB-INF/wsdl/HelloService.wsdl");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      HELLOSERVICE_WSDL_LOCATION = url;
   }
}
