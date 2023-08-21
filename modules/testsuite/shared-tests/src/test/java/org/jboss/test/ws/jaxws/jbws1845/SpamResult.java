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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Custom response object
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 9, 2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpamResult", propOrder = {
    "email",
    "fromAddress",
    "mailDate",
    "complaintDate",
    "mailbox",
    "complainer",
    "xRext",
    "accountName"
})
public final class SpamResult
{
   @XmlElement(required = true, nillable = true)
   protected String email;
   @XmlElement(required = true, nillable = true)
   protected String fromAddress; 
   @XmlElement(required = true, nillable = true)
   protected String mailDate; 
   @XmlElement(required = true, nillable = true)
   protected String complaintDate;
   @XmlElement(required = true, nillable = true)
   protected String mailbox; 
   @XmlElement(required = true, nillable = true)
   protected String complainer; 
   @XmlElement(required = true, nillable = true)
   protected String xRext;
   @XmlElement(required = true, nillable = true)
   protected String accountName;
   
   public SpamResult()
   {
   }
   
   public SpamResult(String email, String fromAddress, String mailDate, String complaintDate, String mailbox, String complainer, String xRext, String accountName)
   {
      this.email = email;
      this.fromAddress = fromAddress;
      this.mailDate = mailDate;
      this.complaintDate = complaintDate;
      this.mailbox = mailbox;
      this.complainer = complainer;
      this.xRext = xRext;
      this.accountName = accountName;
   }
   
   public String[] get()
   {
      return new String[] { email, fromAddress, mailDate, complaintDate, mailbox, complainer, xRext, accountName };
   }
}
