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
package org.jboss.test.ws.jaxws.jbws3140;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "TestEndpointService", targetNamespace = "http://TestEndpoint.org", wsdlLocation = "WEB-INF/wsdl/TestEndpoint.wsdl")
@HandlerChain(file="client-handlers.xml")
public class EndpointService extends Service
{

   private final static URL TESTENDPOINTSERVICE_WSDL_LOCATION;

   private final static WebServiceException TESTENDPOINTSERVICE_EXCEPTION;

   private final static QName TESTENDPOINTSERVICE_QNAME = new QName("http://TestEndpoint.org", "TestEndpointService");

   static
   {
      URL url = null;
      WebServiceException e = null;
      try
      {
         url = new URL("TestEndpoint.wsdl");
      }
      catch (MalformedURLException ex)
      {
         e = new WebServiceException(ex);
      }
      TESTENDPOINTSERVICE_WSDL_LOCATION = url;
      TESTENDPOINTSERVICE_EXCEPTION = e;
   }

   public EndpointService()
   {
      super(__getWsdlLocation(), TESTENDPOINTSERVICE_QNAME);
   }

   public EndpointService(WebServiceFeature... features)
   {
      super(__getWsdlLocation(), TESTENDPOINTSERVICE_QNAME, features);
   }

   public EndpointService(URL wsdlLocation)
   {
      super(wsdlLocation, TESTENDPOINTSERVICE_QNAME);
   }

   public EndpointService(URL wsdlLocation, WebServiceFeature... features)
   {
      super(wsdlLocation, TESTENDPOINTSERVICE_QNAME, features);
   }

   public EndpointService(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public EndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features)
   {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * 
    * @return returns MTOMTest
    */
   @WebEndpoint(name = "MTOMTestPort")
   public MTOMTest getMTOMTestPort()
   {
      return super.getPort(new QName("http://TestEndpoint.org", "MTOMTestPort"), MTOMTest.class);
   }


   /**
    * 
    * @param features
    *            A list of {@link javax.xml.ws.WebServiceFeature} to configure
    *            on the proxy. Supported features not in the
    *            <code>features</code> parameter will have their default
    *            values.
    * @return returns MTOMTest
    */
   @WebEndpoint(name = "MTOMTestPort")
   public MTOMTest getMTOMTestPort(WebServiceFeature... features)
   {
      return super.getPort(new QName("http://TestEndpoint.org", "MTOMTestPort"), MTOMTest.class, features);
   }
   
   
   private static URL __getWsdlLocation()
   {
      if (TESTENDPOINTSERVICE_EXCEPTION != null)
      {
         throw TESTENDPOINTSERVICE_EXCEPTION;
      }
      return TESTENDPOINTSERVICE_WSDL_LOCATION;
   }

}
