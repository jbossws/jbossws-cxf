/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2960;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;

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
