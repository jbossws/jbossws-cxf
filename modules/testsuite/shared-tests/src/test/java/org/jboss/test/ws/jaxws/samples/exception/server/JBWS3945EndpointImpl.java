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

import java.util.Locale;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.soap.SOAPFaultException;

@WebService(endpointInterface = "org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpoint")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class JBWS3945EndpointImpl extends EndpointImpl implements ExceptionEndpoint
{
   @Resource
   private WebServiceContext context;
   
   public void throwSoapFaultException()
   {
      try
      {
         MessageContext ctx = context.getMessageContext();
         ctx.put(MessageContext.HTTP_RESPONSE_CODE, 400);
         
         SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
         SOAPFault fault = factory.createFault();
         fault.addFaultReasonText("this is a fault string!", Locale.ITALIAN);
         fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
         fault.setFaultActor("mr.actor");
         fault.appendFaultSubcode(new QName("http://ws.gss.redhat.com/", "AnException"));
         fault.addDetail().addChildElement("test");
         throw new SOAPFaultException(fault);
      }
      catch (Exception s)
      {
         throw new RuntimeException(s);
      }
   }
}
