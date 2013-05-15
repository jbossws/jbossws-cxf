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

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.ResponseWrapper;

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
