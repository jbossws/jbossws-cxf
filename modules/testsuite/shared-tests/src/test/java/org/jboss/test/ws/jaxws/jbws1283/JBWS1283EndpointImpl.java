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

import org.jboss.ws.api.annotation.WebContext;

import jakarta.jws.WebService;
import jakarta.jws.HandlerChain;
import jakarta.ejb.Stateless;

/**
 * @author Heiko.Braun@jboss.com
 */
@WebService(
		endpointInterface = "org.jboss.test.ws.jaxws.jbws1283.JBWS1283Endpoint",
		serviceName = "JBWS1283Service",
		targetNamespace = "http://org.jboss.test.ws/jbws1283"
	)
@HandlerChain(file = "jaxws-handlers-server.xml")
@WebContext(contextRoot = "jaxws-jbws1283")
@Stateless
public class JBWS1283EndpointImpl implements JBWS1283Endpoint
{

	public void requestAttachmentData()
	{
		System.out.println("AttachmentData will be added within Handler.handleResponse()");
	}
}
