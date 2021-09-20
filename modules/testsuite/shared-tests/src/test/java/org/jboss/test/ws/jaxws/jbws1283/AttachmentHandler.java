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
package org.jboss.test.ws.jaxws.jbws1283;

import jakarta.activation.DataHandler;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * Appends attachments to outbound message.
 *
 * @author Heiko.Braun@jboss.com
 */
public class AttachmentHandler extends GenericSOAPHandler<SOAPMessageContext>
{
	@Override
   protected boolean handleOutbound(SOAPMessageContext msgContext)
   {
		System.out.println("AttachmentHandler: Adding attachment ot message");
		SOAPMessage soapMessage = msgContext.getMessage();
		AttachmentPart attachment = soapMessage.createAttachmentPart(new DataHandler("Server data", "text/plain"));
		soapMessage.addAttachmentPart(attachment);
		return true;
   }
}
