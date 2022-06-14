/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1694;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.ParameterStyle;
import jakarta.xml.ws.Holder;

/**
 * @author Heiko.Braun@jboss.com
 */
@WebService
@Remote
//BARE required as the header param forces wsdl:message to have more than one wsdl:part thus WRAPPED (which is default) should not be allowed
@SOAPBinding(parameterStyle=ParameterStyle.BARE)
public interface JBWS1694EndpointSEI
{
   @WebMethod(operationName = "SubmitBasket")
    @WebResult(name = "receipt", targetNamespace = "http://www.m-bar-go.com", partName = "response")
    public Receipt submitBasket(

           @WebParam(name = "inout", targetNamespace = "http://www.m-bar-go.com", header = true,
               mode = WebParam.Mode.INOUT)
               Holder<Header> header,

            @WebParam(name = "basket", targetNamespace = "http://www.m-bar-go.com", partName = "request")
               Basket request) 
      throws Exception;
}
