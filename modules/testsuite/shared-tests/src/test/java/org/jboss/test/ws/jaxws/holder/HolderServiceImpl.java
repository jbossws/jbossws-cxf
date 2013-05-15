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
package org.jboss.test.ws.jaxws.holder;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

/**
 * A service which tests JAX-WS Holder types
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
@WebService(name="Holder")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class HolderServiceImpl
{
   public Long echoOuts(
         @WebParam(name = "in1") Integer in1, 
         @WebParam(name = "in2") String in2, 
         @WebParam(name = "in3") Long in3, 
         @WebParam(name = "out1", mode = WebParam.Mode.OUT) Holder<Integer> out1, 
         @WebParam(name = "out2", mode = WebParam.Mode.OUT) Holder<String> out2)
   {
      out1.value = in1;
      out2.value = in2;
      return in3;
   }
   
   public Long echoInOuts(
         @WebParam(name = "in1") Long in1, 
         @WebParam(name = "inout1", mode = WebParam.Mode.INOUT) Holder<Integer> inout1, 
         @WebParam(name = "inout2", mode = WebParam.Mode.INOUT) Holder<String> inout2)
   {
      return in1;
   }
   
   public Long echoMixed(
         @WebParam(name = "in1") Integer in1, 
         @WebParam(name = "in2") String in2,
         @WebParam(name = "inout1", mode = WebParam.Mode.INOUT) Holder<Integer> inout1, 
         @WebParam(name = "inout2", mode = WebParam.Mode.INOUT) Holder<String> inout2,
         @WebParam(name = "in3") Long in3, 
         @WebParam(name = "out1", mode = WebParam.Mode.OUT) Holder<Integer> out1, 
         @WebParam(name = "out2", mode = WebParam.Mode.OUT) Holder<String> out2)
   {
      out1.value = in1;
      out2.value = in2;
      return in3;
   }
   
   @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
   public void echoBareOut(
         @WebParam(name ="in") String in,
         @WebParam(name ="out", mode=WebParam.Mode.OUT) Holder<String> out)
   {
      out.value = in;
   }
   
   @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
   public void echoBareInOut(
         @WebParam(name ="inout", mode=WebParam.Mode.INOUT) Holder<String> inout)
   {
   }
   
   public void addInOut(
         @WebParam(name ="sum", mode=WebParam.Mode.INOUT) Holder<Integer> sum,
         @WebParam(name ="add") int add)
   {
      sum.value = sum.value.intValue() + add;
   }
}
