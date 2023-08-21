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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "EndpointService", targetNamespace = "http://org.jboss.ws/bus", wsdlLocation = "file://bogus-location/jaxws-cxf-busf?wsdl")
public class EndpointService
    extends Service
{

    private final static URL WSDL_LOCATION;
    private final static QName TESTENDPOINTSERVICE = new QName("http://org.jboss.ws/bus", "EndpointService");
    private final static QName TESTENDPOINTPORT = new QName("http://org.jboss.ws/bus", "EndpointPort");

    static {
        URL url = null;
        try {
            url = new URL("file://bogus-location/jaxws-cxf-bus?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public EndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
       super(wsdlLocation, serviceName, features);
    }

    public EndpointService() {
        super(WSDL_LOCATION, TESTENDPOINTSERVICE);
    }

    /**
     * 
     * @return
     *     returns Endpoint
     */
    @WebEndpoint(name = "EndpointPort")
    public Endpoint getEndpointPort() {
        return (Endpoint)super.getPort(TESTENDPOINTPORT, Endpoint.class);
    }
    
    /**
     * 
     * @return
     *     returns Endpoint
     */
    @WebEndpoint(name = "EndpointPort")
    public Endpoint getEndpointPort(WebServiceFeature... features) {
        return (Endpoint)super.getPort(TESTENDPOINTPORT, Endpoint.class, features);
    }

}
