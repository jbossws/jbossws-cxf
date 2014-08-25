/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1581;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name="EndpointService", targetNamespace="http://jbws1581.jaxws.ws.test.jboss.org/")
public class EndpointService extends javax.xml.ws.Service
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
