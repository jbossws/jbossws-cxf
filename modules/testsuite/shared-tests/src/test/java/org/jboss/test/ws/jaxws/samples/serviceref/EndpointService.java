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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

/**
 * JBossWS Generated Source
 * 
 * Generation Date: Mon Mar 12 15:09:39 CET 2007
 * 
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 * 
 * JAX-WS Version: 2.0
 * 
 */
@WebServiceClient(name = "EndpointService", targetNamespace = "http://serviceref.samples.jaxws.ws.test.jboss.org/", wsdlLocation = "http://tddell:8080/jaxws-samples-serviceref?wsdl")
public class EndpointService
    extends Service implements Serializable
{

   private static final long serialVersionUID = 1L;
   private final static URL TESTENDPOINTSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/jaxws-samples-serviceref?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TESTENDPOINTSERVICE_WSDL_LOCATION = url;
    }

    public EndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
       super(wsdlLocation, serviceName, features);
   }

    public EndpointService() {
        super(TESTENDPOINTSERVICE_WSDL_LOCATION, new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointService"));
    }

    /**
     * 
     * @return
     *     returns Endpoint
     */
    @WebEndpoint(name = "EndpointPort")
    public Endpoint getEndpointPort() {
        return (Endpoint)super.getPort(new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointPort"), Endpoint.class);
    }

}
