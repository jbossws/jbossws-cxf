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
package org.jboss.test.ws.jaxws.jbws1904;

import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.ws.WebFault;

@WebFault
(
   name="UserExceptionFault",
   targetNamespace="http://org.jboss.ws/jbws1904/faults"
)
@XmlType
(
   name = "UserException",
   namespace = "http://org.jboss.ws/jbws1904/exceptions",
   propOrder = {"message"}
)
public class UserException extends Exception
{
   private static final long serialVersionUID = 3203477602591432846L;
   
   private String message;

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }
}
