/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;


import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;


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

