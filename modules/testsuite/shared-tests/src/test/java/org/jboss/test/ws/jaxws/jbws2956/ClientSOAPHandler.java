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
package org.jboss.test.ws.jaxws.jbws2956;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.ws.api.handler.GenericSOAPHandler;

public class ClientSOAPHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   @Override
   protected boolean handleInbound(final SOAPMessageContext msgContext)
   {
      //do nothing
      return true;
   }

   protected boolean handleOutbound(final SOAPMessageContext msgContext)
   {
      try
      {
         SOAPFault fault = null;
         MessageFactory factory = MessageFactory.newInstance(); 
         SOAPMessage resMessage = factory.createMessage();
         fault = resMessage.getSOAPBody().addFault();
         fault.setFaultString("this is an exception thrown by client outbound");
         throw new SOAPFaultException(fault);
      }
      catch (SOAPException e)
      {
         //ignore
      }
      return true;
   }
}
