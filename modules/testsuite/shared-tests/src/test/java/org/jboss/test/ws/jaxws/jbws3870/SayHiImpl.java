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
package org.jboss.test.ws.jaxws.jbws3870;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://apache.org/sayHi", name = "SayHi", wsdlLocation = "WEB-INF/classes/wsdl/thewsdl/sayHi.wsdl", endpointInterface = "org.jboss.test.ws.jaxws.jbws3870.SayHi")
public class SayHiImpl implements SayHi
{

   @Override
   @WebResult(name = "return", targetNamespace = "")
   @RequestWrapper(localName = "sayHiArray", targetNamespace = "http://apache.org/sayHi2", className = "org.jboss.sayhi2.SayHiArray")
   @WebMethod
   @ResponseWrapper(localName = "sayHiArrayResponse", targetNamespace = "http://apache.org/sayHi2", className = "org.jboss.sayhi2.SayHiArrayResponse")
   public List<String> sayHiArray(@WebParam(name = "arg0", targetNamespace = "") List<String> arg0)
   {
      List<String> list = new ArrayList<String>();
      list.add("Hi");
      return list;
   }

   @Override
   @WebResult(name = "return", targetNamespace = "http://apache.org/sayHi1")
   @RequestWrapper(localName = "sayHi", targetNamespace = "http://apache.org/sayHi1", className = "org.jboss.sayhi1.SayHi")
   @WebMethod
   @ResponseWrapper(localName = "sayHiResponse", targetNamespace = "http://apache.org/sayHi1", className = "org.jboss.sayhi1.SayHiResponse")
   public String sayHi(@WebParam(name = "arg0", targetNamespace = "http://apache.org/sayHi1") String arg0)
   {
      return "Hi";
   }

}
