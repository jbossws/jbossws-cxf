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
package org.jboss.test.ws.jaxws.jbws1845;

import jakarta.ejb.Remote;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.ResponseWrapper;

/**
 * Webservice iface
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
@Remote
@WebService
public interface SpamComplaintWSIface
{
   @WebMethod(operationName="processSpamComplaints")
   @WebResult(name="SpamResult")
   @ResponseWrapper(className="org.jboss.test.ws.jaxws.jbws1845.jaxws.SpamResult")
   public SpamResult processSpamComplaints(
      @WebParam(name = "email") String email,
      @WebParam(name = "fromAddress") String fromAddress,
      @WebParam(name = "mailDate") String mailDate,
      @WebParam(name = "complaintDate") String complaintDate,
      @WebParam(name = "mailbox") String mailbox,
      @WebParam(name = "complainer") String complainer,
      @WebParam(name = "xRext") String xRext,
      @WebParam(name = "accountName") String accountName
   );
}
