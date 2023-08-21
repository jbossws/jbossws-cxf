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
package org.jboss.test.ws.jaxws.jbws3477;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.spi.ServiceDelegate;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

public class DummyProvider extends jakarta.xml.ws.spi.Provider
{
   @Override
   public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class<? extends Service> serviceClass)
   {
      return null;
   }

   @Override
   public Endpoint createEndpoint(String bindingId, Object implementor)
   {
      return null;
   }

   @Override
   public Endpoint createAndPublishEndpoint(String address, Object implementor)
   {
      return null;
   }

   @Override
   public EndpointReference readEndpointReference(Source eprInfoset)
   {
      return null;
   }

   @Override
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface,
         WebServiceFeature... features)
   {
      return null;
   }

   @Override
   public W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName,
         List<Element> metadata, String wsdlDocumentLocation, List<Element> referenceParameters)
   {
      return null;
   }
}
