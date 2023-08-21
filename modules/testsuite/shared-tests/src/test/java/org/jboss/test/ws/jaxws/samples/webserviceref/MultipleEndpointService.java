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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "EndpointService", targetNamespace = "http://org.jboss.ws/wsref", wsdlLocation = "META-INF/wsdl/MultipleEndpoint.wsdl")
public class MultipleEndpointService extends Service
{

   private final static URL ENDPOINTSERVICE_WSDL_LOCATION;
   private final static WebServiceException ENDPOINTSERVICE_EXCEPTION;
   private final static QName ENDPOINTSERVICE_QNAME = new QName("http://org.jboss.ws/wsref", "EndpointService");

   static
   {
      URL url = null;
      WebServiceException e = null;
      url = MultipleEndpointService.class.getResource("bogusAddress"); //invalid address on purpose, to test JBWS-3015 via service7 in EJB3Client
      if (url == null)
      {
         e = new WebServiceException("Cannot find wsdl, please put in classpath");
      }
      ENDPOINTSERVICE_WSDL_LOCATION = url;
      ENDPOINTSERVICE_EXCEPTION = e;
   }

   public MultipleEndpointService()
   {
      super(__getWsdlLocation(), ENDPOINTSERVICE_QNAME);
   }

   public MultipleEndpointService(URL wsdlLocation)
   {
      super(wsdlLocation, ENDPOINTSERVICE_QNAME);
   }

   public MultipleEndpointService(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public MultipleEndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features)
   {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * 
    * @return
    *     returns Endpoint
    */
   @WebEndpoint(name = "EndpointPort")
   public Endpoint getEndpointPort()
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort"), Endpoint.class);
   }

   @WebEndpoint(name = "EndpointPort2")
   public Endpoint getEndpointPort2()
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort2"), Endpoint.class);
   }

   /**
    * 
    * @param features
    *     A list of {@link jakarta.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
    * @return
    *     returns Endpoint
    */
   @WebEndpoint(name = "EndpointPort")
   public Endpoint getEndpointPort(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort"), Endpoint.class, features);
   }

   @WebEndpoint(name = "EndpointPort2")
   public Endpoint getEndpointPort2(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://org.jboss.ws/wsref", "EndpointPort2"), Endpoint.class, features);
   }

   private static URL __getWsdlLocation()
   {
      if (ENDPOINTSERVICE_EXCEPTION != null)
      {
         throw ENDPOINTSERVICE_EXCEPTION;
      }
      return ENDPOINTSERVICE_WSDL_LOCATION;
   }

}
