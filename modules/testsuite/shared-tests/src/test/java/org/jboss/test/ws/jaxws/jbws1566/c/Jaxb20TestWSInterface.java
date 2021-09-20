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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;
import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;

@jakarta.jws.WebService(targetNamespace = "http://org.jboss.ws/samples/c")
@jakarta.jws.soap.SOAPBinding(style = jakarta.jws.soap.SOAPBinding.Style.DOCUMENT, use = jakarta.jws.soap.SOAPBinding.Use.LITERAL, parameterStyle = jakarta.jws.soap.SOAPBinding.ParameterStyle.WRAPPED)
public interface Jaxb20TestWSInterface extends java.rmi.Remote
{

   @jakarta.jws.WebMethod(operationName = "Method")
   public @jakarta.jws.WebResult(name = "result")
   TestEnumeration testMethod(@jakarta.jws.WebParam(name = "input", mode = jakarta.jws.WebParam.Mode.IN)
   BClass input) throws BException, RemoteException;

}
