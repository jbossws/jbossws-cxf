/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2307;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "HelloService", targetNamespace = "http://helloservice.org/wsdl", wsdlLocation = "WEB-INF/wsdl/HelloService.wsdl")
public class HelloServiceJAXWS22 extends Service
{
   private static final URL HELLOSERVICE_WSDL_LOCATION;

   public HelloServiceJAXWS22(URL wsdlLocation, QName serviceName)
   {
      super(wsdlLocation, serviceName);
   }

   public HelloServiceJAXWS22()
   {
      super(HELLOSERVICE_WSDL_LOCATION, new QName("http://helloservice.org/wsdl", "HelloService"));
   }
   
   public HelloServiceJAXWS22(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
      super(wsdlLocation, serviceName, features);
   }

   @WebEndpoint(name = "HelloPort")
   public Hello getHelloPort()
   {
      return ((Hello)super.getPort(new QName("http://helloservice.org/wsdl", "HelloPort"), Hello.class));
   }

   static
   {
      URL url = null;
      try
      {
         url = new URL("http://files1/releng/cts_5.x/cts-5.0c-temp/bin/WEB-INF/wsdl/HelloService.wsdl");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      HELLOSERVICE_WSDL_LOCATION = url;
   }
}
