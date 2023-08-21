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
package org.jboss.test.ws.jaxws.jbws2960;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.FaultAction;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.Addressing;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.SOAPBinding;

@WebService(name = "AddNumbers", portName = "AddNumbersPort", targetNamespace = "http://foobar.org/", serviceName = "AddNumbersService")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@Addressing(responses = AddressingFeature.Responses.NON_ANONYMOUS)
public class AddNumbersImpl
{

   public int addNumbersNoAction(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "", output = "")
   public int addNumbersEmptyAction(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "http://example.com/input", output = "http://example.com/output")
   @WebMethod(action = "http://example.com/input")
   public int addNumbers(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "http://example.com/input2", output = "http://example.com/output2")
   public int addNumbers2(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @WebMethod(action = "http://example.com/input3")
   public int addNumbers3(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "http://example.com/input4")
   public int addNumbers4(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "finput1", output = "foutput1", fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://fault1")})
   public int addNumbersFault1(int number1, int number2) throws AddNumbersException
   {
      return -1;
   }

   @Action(input = "finput2", output = "foutput2", fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://fault2/addnumbers"),
         @FaultAction(className = TooBigNumbersException.class, value = "http://fault2/toobignumbers")})
   public int addNumbersFault2(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }

   @Action(input = "finput3", output = "foutput3", fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://fault3/addnumbers")})
   public int addNumbersFault3(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }

   @Action(fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://fault4/addnumbers")})
   public int addNumbersFault4(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }

   @Action(fault =
   {@FaultAction(className = TooBigNumbersException.class, value = "http://fault5/toobignumbers")})
   public int addNumbersFault5(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }

   @Action(fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://fault6/addnumbers"),
         @FaultAction(className = TooBigNumbersException.class, value = "http://fault6/toobignumbers")})
   public int addNumbersFault6(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }

   @Action(fault =
   {@FaultAction(className = AddNumbersException.class, value = ""),
         @FaultAction(className = TooBigNumbersException.class, value = "")})
   public int addNumbersFault7(int number1, int number2) throws AddNumbersException, TooBigNumbersException
   {
      return -1;
   }
}
