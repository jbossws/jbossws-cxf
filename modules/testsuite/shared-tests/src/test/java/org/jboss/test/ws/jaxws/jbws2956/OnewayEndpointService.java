/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.jbws2956;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;

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
