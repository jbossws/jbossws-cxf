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
package org.jboss.test.ws.jaxws.jbws3140;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.jws.HandlerChain;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "TestEndpointService", targetNamespace = "http://TestEndpoint.org", wsdlLocation = "WEB-INF/wsdl/TestEndpoint.wsdl")
@HandlerChain(file="client-handlers.xml")
public class EndpointService extends Service
{

   private final static URL TESTENDPOINTSERVICE_WSDL_LOCATION;

   private final static WebServiceException TESTENDPOINTSERVICE_EXCEPTION;

   private final static QName TESTENDPOINTSERVICE_QNAME = new QName("http://TestEndpoint.org", "TestEndpointService");

   static
   {
      URL url = null;
      WebServiceException e = null;
      try
      {
         url = new URL("TestEndpoint.wsdl");
      }
      catch (MalformedURLException ex)
      {
         e = new WebServiceException(ex);
      }
      TESTENDPOINTSERVICE_WSDL_LOCATION = url;
      TESTENDPOINTSERVICE_EXCEPTION = e;
   }

   public EndpointService()
   {
      super(__getWsdlLocation(), TESTENDPOINTSERVICE_QNAME);
   }

   public EndpointService(WebServiceFeature... features)
   {
      super(__getWsdlLocation(), TESTENDPOINTSERVICE_QNAME, features);
   }

   public EndpointService(URL wsdlLocation)
   {
      super(wsdlLocation, TESTENDPOINTSERVICE_QNAME);
   }

   public EndpointService(URL wsdlLocation, WebServiceFeature... features)
   {
      super(wsdlLocation, TESTENDPOINTSERVICE_QNAME, features);
   }

   public EndpointService(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public EndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features)
   {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * 
    * @return returns MTOMTest
    */
   @WebEndpoint(name = "MTOMTestPort")
   public MTOMTest getMTOMTestPort()
   {
      return super.getPort(new QName("http://TestEndpoint.org", "MTOMTestPort"), MTOMTest.class);
   }


   /**
    * 
    * @param features
    *            A list of {@link jakarta.xml.ws.WebServiceFeature} to configure
    *            on the proxy. Supported features not in the
    *            <code>features</code> parameter will have their default
    *            values.
    * @return returns MTOMTest
    */
   @WebEndpoint(name = "MTOMTestPort")
   public MTOMTest getMTOMTestPort(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://TestEndpoint.org", "MTOMTestPort"), MTOMTest.class, features);
   }
   
   
   private static URL __getWsdlLocation()
   {
      if (TESTENDPOINTSERVICE_EXCEPTION != null)
      {
         throw TESTENDPOINTSERVICE_EXCEPTION;
      }
      return TESTENDPOINTSERVICE_WSDL_LOCATION;
   }

}
