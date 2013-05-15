/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1845;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
