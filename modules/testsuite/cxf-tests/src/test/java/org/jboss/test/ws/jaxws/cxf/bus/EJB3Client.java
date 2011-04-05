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
package org.jboss.test.ws.jaxws.cxf.bus;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;

/**
 * This class verifies the default bus is not changed by
 * basic client use (creation of bus through BusFactory.newInstance().createBus(),
 * SAAJ invocation, endpoint invocation, endpoint invocation using webserviceref).
 * 
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2010
 *
 */
@Stateless
@Remote(EJB3ClientRemoteInterface.class)
public class EJB3Client
{
   @WebServiceRef(value = EndpointService.class, type = Endpoint.class, wsdlLocation = "META-INF/wsdl/Endpoint.wsdl")
   public Endpoint port;
   
   public void testBusCreation() throws BusTestException
   {
      AbstractClient.testBusCreation();
   }
   
   public void testSOAPConnection(String host) throws BusTestException, Exception
   {
      AbstractClient.testSOAPConnection(host);
   }
   
   public void testWebServiceRef() throws BusTestException
   {
      AbstractClient.testWebServiceRef(port);
   }
   
   public void testWebServiceClient(String host) throws BusTestException, Exception
   {
      AbstractClient.testWebServiceClient(host);
   }
}
