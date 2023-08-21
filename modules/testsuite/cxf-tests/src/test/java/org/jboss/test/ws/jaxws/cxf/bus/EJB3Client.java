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

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;

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
   
   public void testSOAPConnection(String host) throws BusTestException
   {
      try {
         AbstractClient.testSOAPConnection(host);
      } catch (Exception e) {
         Logger.getLogger(this.getClass()).error("Could not run 'testSOAPConnection'", e);
         throw new BusTestException(e.getMessage());
      }
   }
   
   public void testWebServiceRef() throws BusTestException
   {
      AbstractClient.testWebServiceRef(port);
   }
   
   public void testWebServiceClient(String host) throws BusTestException
   {
      try {
         AbstractClient.testWebServiceClient(host);
      } catch (Exception e) {
         Logger.getLogger(this.getClass()).error("Could not run 'testWebServiceClient'", e);
         throw new BusTestException(e.getMessage());
      }
   }
}
