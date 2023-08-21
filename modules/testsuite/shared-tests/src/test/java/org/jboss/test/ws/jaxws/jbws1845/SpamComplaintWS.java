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

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.xml.ws.ResponseWrapper;

import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;

/**
 * Webservice impl
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
@Stateless
@WebService
(
   serviceName = "SpamService",
   targetNamespace = "http://service.responsys.com/rsystools/ws/SpamComplaintWS/1.0",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws1845.SpamComplaintWSIface"
)
@WebContext
(
   transportGuarantee = TransportGuarantee.NONE,
   contextRoot = "/jaxws-jbws1845",
   urlPattern = "/SpamService"
)
public class SpamComplaintWS implements SpamComplaintWSIface
{
   @ResponseWrapper(className="org.jboss.test.ws.jaxws.jbws1845.jaxws.SpamResult")
   public SpamResult processSpamComplaints(
       String email,
       String fromAddress,
       String mailDate,
       String complaintDate,
       String mailbox,
       String complainer,
       String xRext,
       String accountName)
   {
      return new SpamResult(email, fromAddress, mailDate, complaintDate, mailbox, complainer, xRext, accountName);
   }
}
