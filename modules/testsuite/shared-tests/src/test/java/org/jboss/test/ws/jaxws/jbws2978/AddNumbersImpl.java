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
package org.jboss.test.ws.jaxws.jbws2978;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.soap.Addressing;
@WebService(name = "AddNumbers", portName = "AddNumbersPort", serviceName = "AddNumbers", targetNamespace = "http://ws.jboss.org")
@Addressing(enabled = true, required = true)
public class AddNumbersImpl
{

   @Action(input = "inputAction", output = "outputAction")
   public int addNumbers2(@WebParam(name = "number1") int number1, @WebParam(name = "number2") int number2)
   {
      return number1 + number2;
   }
}
