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
package org.jboss.test.ws.jaxws.jbws1581;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name="EndpointService", targetNamespace="http://jbws1581.jaxws.ws.test.jboss.org/")
public class EndpointService extends jakarta.xml.ws.Service
{
   private final static QName TESTENDPOINTPORT = new QName("http://jbws1581.jaxws.ws.test.jboss.org/", "EndpointBeanPort");
   
   public EndpointService(URL wsdlLocation, QName serviceName) {
      super(wsdlLocation, serviceName);
   }
   
   public EndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
      super(wsdlLocation, serviceName, features);
   }
   
   @WebEndpoint(name = "EndpointBeanPort")
   public EndpointInterface getEndpointPort() {
      return (EndpointInterface)super.getPort(TESTENDPOINTPORT, EndpointInterface.class);
   }
}
