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
package org.jboss.test.ws.jaxws.holder;

import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.Holder;

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
