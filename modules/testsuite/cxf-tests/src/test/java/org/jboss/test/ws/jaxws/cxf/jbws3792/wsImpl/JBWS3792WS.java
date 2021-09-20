/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;


import jakarta.jws.WebMethod;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://test.jbws3792/", name = "JBWS3792WS")
public interface JBWS3792WS {

   @WebResult(name = "return", targetNamespace = "")
   @RequestWrapper(localName = "hello", targetNamespace = "http://test.jbws3792/",
      className = "org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.Hello")
   @WebMethod
   @ResponseWrapper(localName = "helloResponse",
      targetNamespace = "http://test.jbws3792/",
      className = "org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.HelloResponse")
   public java.lang.String hello();
}
