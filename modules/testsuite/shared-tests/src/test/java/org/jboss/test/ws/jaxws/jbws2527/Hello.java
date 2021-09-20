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
package org.jboss.test.ws.jaxws.jbws2527;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService(name="Hello", targetNamespace="http://helloservice.org/wsdl")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public abstract interface Hello
{
  @WebMethod
  @WebResult(name="result", partName="result")
  public abstract String hello(@WebParam(name="String_1", partName="String_1") String paramString);

  @WebMethod
  @WebResult(name="result", partName="result")
  public abstract boolean getMessageContextTest();

  @WebMethod
  @WebResult(name="result", partName="result")
  public abstract boolean getServletContextTest();

  @WebMethod
  @WebResult(name="result", partName="result")
  public abstract boolean getUserPrincipalTest();

  @WebMethod
  @WebResult(name="result", partName="result")
  public abstract boolean isUserInRoleTest(@WebParam(name="String_1", partName="String_1") String paramString);
}