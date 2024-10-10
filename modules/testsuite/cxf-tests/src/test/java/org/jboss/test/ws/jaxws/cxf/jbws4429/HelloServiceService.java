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
package org.jboss.test.ws.jaxws.cxf.jbws4429;

import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "HelloServiceService", targetNamespace = "http://com.redhat.gss.example.soap/", wsdlLocation = "HelloServiceService.wsdl")
public class HelloServiceService
        extends Service
{

    private final static QName HELLOSERVICESERVICE_QNAME = new QName("http://com.redhat.gss.example.soap/", "HelloServiceService");

    public HelloServiceService(URL wsdlLocation) {
        super(wsdlLocation, HELLOSERVICESERVICE_QNAME);
    }

    /**
     *
     * @return
     *     returns HelloService
     */
    @WebEndpoint(name = "HelloServicePort")
    public HelloService getHelloServicePort() {
        return super.getPort(new QName("http://com.redhat.gss.example.soap/", "HelloServicePort"), HelloService.class);
    }

    /**
     *
     * @param features
     *     A list of {@link jakarta.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns HelloService
     */
    @WebEndpoint(name = "HelloServicePort")
    public HelloService getHelloServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://com.redhat.gss.example.soap/", "HelloServicePort"), HelloService.class, features);
    }

}

