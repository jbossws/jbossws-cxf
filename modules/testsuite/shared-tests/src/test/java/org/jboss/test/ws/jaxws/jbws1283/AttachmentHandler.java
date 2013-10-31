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

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * Appends attachments to outbound message.
 *
 * @author Heiko.Braun@jboss.com
 */
public class AttachmentHandler extends GenericSOAPHandler<LogicalMessageContext>
{
	@Override
   protected boolean handleOutbound(MessageContext msgContext)
   {
		System.out.println("AttachmentHandler: Adding attachment ot message");
		SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
		AttachmentPart attachment = soapMessage.createAttachmentPart(new DataHandler("Server data", "text/plain"));
		soapMessage.addAttachmentPart(attachment);
		return true;
   }
}
