/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.exception;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.test.ws.jaxws.samples.exception.client.ExceptionEndpoint;

public class SOAP12ExceptionEJB3Helper extends SOAP12ExceptionHelper
{
   public SOAP12ExceptionEJB3Helper(String targetEndpoint)
   {
      super(targetEndpoint);
   }
   
   public SOAP12ExceptionEJB3Helper()
   {
      super();
   }
   
   protected ExceptionEndpoint getProxy() throws Exception
   {
      QName serviceName = new QName(targetNS, "SOAP12ExceptionEndpointEJB3ImplService");
      URL wsdlURL = new URL(targetEndpoint + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(ExceptionEndpoint.class);
   }
}
