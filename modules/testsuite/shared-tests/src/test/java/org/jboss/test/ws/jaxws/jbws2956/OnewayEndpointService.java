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
package org.jboss.test.ws.jaxws.jbws2956;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.jws.HandlerChain;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceException;

@WebServiceClient(name = "EndpointService", targetNamespace = "http://ws.jboss.org/jbws2956")
@HandlerChain(file = "client-handlers.xml")
public class OnewayEndpointService
    extends Service
{

    private final static URL ENDPOINTSERVICE_WSDL_LOCATION;
    private final static WebServiceException ENDPOINTSERVICE_EXCEPTION;
    private final static QName ENDPOINTSERVICE_QNAME = new QName("http://ws.jboss.org/jbws2956", "EndpointService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/jaxws-jbws2955?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        ENDPOINTSERVICE_WSDL_LOCATION = url;
        ENDPOINTSERVICE_EXCEPTION = e;
    }

    public OnewayEndpointService() {
        super(__getWsdlLocation(), ENDPOINTSERVICE_QNAME);
    }


    public OnewayEndpointService(URL wsdlLocation) {
        super(wsdlLocation, ENDPOINTSERVICE_QNAME);
    }

    public OnewayEndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    /**
     * 
     * @return
     *     returns Endpoint
     */
    @WebEndpoint(name = "EndpointPort")
    public OnewayEndpoint getOnewayEndpointPort() {
        return super.getPort(new QName("http://ws.jboss.org/jbws2956", "EndpointPort"), OnewayEndpoint.class);
    }

    private static URL __getWsdlLocation() {
        if (ENDPOINTSERVICE_EXCEPTION!= null) {
            throw ENDPOINTSERVICE_EXCEPTION;
        }
        return ENDPOINTSERVICE_WSDL_LOCATION;
    }

}
