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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;


import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.Service;


@WebServiceClient(name = "JBWS3792WSService",
   wsdlLocation = "http://localhost:8080/jbws3792-external-wsdl/jbws3792.wsdl",
   targetNamespace = "http://test.jbws3792/")
public class JBWS3792WSService extends Service {

   public final static URL WSDL_LOCATION;

   public final static QName SERVICE = new QName("http://test.jbws3792/", "JBWS3792WSService");
   public final static QName JBWS3792WSPort = new QName("http://test.jbws3792/", "JBWS3792WSPort");
   static {
      URL url = null;
      try {
         url = new URL("http://localhost:8080/jbws3792-external-wsdl/jbws3792.wsdl");
      } catch (MalformedURLException e) {
         java.util.logging.Logger.getLogger(JBWS3792WSService.class.getName())
            .log(java.util.logging.Level.INFO,
               "Can not initialize the default wsdl from {0}", "http://localhost:8080/jbws3792-external-wsdl/jbws3792.wsdl");
      }
      WSDL_LOCATION = url;
   }

   public JBWS3792WSService(URL wsdlLocation) {
      super(wsdlLocation, SERVICE);
   }

   public JBWS3792WSService(URL wsdlLocation, QName serviceName) {
      super(wsdlLocation, serviceName);
   }

   public JBWS3792WSService() {
      super(WSDL_LOCATION, SERVICE);
   }

   public JBWS3792WSService(WebServiceFeature ... features) {
      super(WSDL_LOCATION, SERVICE, features);
   }

   public JBWS3792WSService(URL wsdlLocation, WebServiceFeature ... features) {
      super(wsdlLocation, SERVICE, features);
   }

   public JBWS3792WSService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
      super(wsdlLocation, serviceName, features);
   }

   @WebEndpoint(name = "JBWS3792WSPort")
   public JBWS3792WS getJBWS3792WSPort() {
      return super.getPort(JBWS3792WSPort, JBWS3792WS.class);
   }


   @WebEndpoint(name = "JBWS3792WSPort")
   public JBWS3792WS getJBWS3792WSPort(WebServiceFeature... features) {
      return super.getPort(JBWS3792WSPort, JBWS3792WS.class, features);
   }

}

