/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.exception.server;

import java.util.Locale;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

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
