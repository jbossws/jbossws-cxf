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
package org.jboss.test.ws.jaxws.jbws1799;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

/**
 * Second service interface
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 8, 2007
 */
@Remote
@WebService
@SOAPBinding
(
      style=SOAPBinding.Style.DOCUMENT,
      use=SOAPBinding.Use.LITERAL
)
public interface IUserAccountServiceExt
{
   @WebMethod
   @RequestWrapper(className="org.jboss.test.ws.jaxws.jbws1799.jaxws.Authenticate1")
   @ResponseWrapper(className="org.jboss.test.ws.jaxws.jbws1799.jaxws.Authenticate1Response")
   public boolean authenticate
   (
         @WebParam(name="username") String username,
         @WebParam(name="password") String password
   );
}
