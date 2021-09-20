/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3477;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.spi.ServiceDelegate;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

public class DummyProvider extends jakarta.xml.ws.spi.Provider
{
   @Override
   public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class<? extends Service> serviceClass)
   {
      return null;
   }

   @Override
   public Endpoint createEndpoint(String bindingId, Object implementor)
   {
      return null;
   }

   @Override
   public Endpoint createAndPublishEndpoint(String address, Object implementor)
   {
      return null;
   }

   @Override
   public EndpointReference readEndpointReference(Source eprInfoset)
   {
      return null;
   }

   @Override
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface,
         WebServiceFeature... features)
   {
      return null;
   }

   @Override
   public W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName,
         List<Element> metadata, String wsdlDocumentLocation, List<Element> referenceParameters)
   {
      return null;
   }
}
