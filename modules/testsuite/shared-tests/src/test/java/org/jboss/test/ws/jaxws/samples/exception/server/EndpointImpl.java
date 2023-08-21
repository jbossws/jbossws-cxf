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
package org.jboss.test.ws.jaxws.samples.exception.server;

import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;

public class EndpointImpl
{
   public void throwRuntimeException()
   {
      throw new RuntimeException("oh no, a runtime exception occured.");
   }

   public void throwSoapFaultException()
   {
      // This should be thrown as-is
      try
      {
         SOAPFactory factory = SOAPFactory.newInstance();
         SOAPFault fault = factory.createFault("this is a fault string!", SOAPConstants.SOAP_VERSIONMISMATCH_FAULT);
         fault.setFaultActor("mr.actor");
         fault.addDetail().addChildElement("test");
         throw new SOAPFaultException(fault);
      }
      catch (SOAPException s)
      {
         throw new RuntimeException(s);
      }
   }

   public void throwApplicationException() throws UserException
   {
      throw new UserException("validation", 123, "Some validation error");
   }
}
