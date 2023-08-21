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
package org.jboss.test.ws.jaxws.samples.exception.client;

import jakarta.xml.ws.WebFault;


@WebFault(name = "UserException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/")
public class UserException_Exception
    extends Exception
{

   private static final long serialVersionUID = -2070541986440051888L;
   /**
    * Java type that goes as soapenv:Fault detail element.
    *
    */
   private final UserException faultInfo;

   /**
    *
    * @param faultInfo
    * @param message
    */
   public UserException_Exception(String message, UserException faultInfo) {
      super(message);
      this.faultInfo = faultInfo;
   }

   /**
    *
    * @param faultInfo
    * @param message
    * @param cause
    */
   public UserException_Exception(String message, UserException faultInfo, Throwable cause) {
      super(message, cause);
      this.faultInfo = faultInfo;
   }

   /**
    *
    * @return
    *     returns fault bean: org.jboss.test.ws.jaxws.samples.exception.client.UserException
    */
   public UserException getFaultInfo() {
      return faultInfo;
   }

}
