/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.jaxrs.boot.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
/**
 * 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
@Provider
public class SetPropertyProvider implements ClientRequestFilter {
	private AtomicInteger counter;
	
	public SetPropertyProvider(AtomicInteger counter) {
		super();
		this.counter = counter;
	}

	public void filter(ClientRequestContext context) throws IOException {
		if (counter.incrementAndGet() == 2) {
			Response response = Response.ok("Value is set from client provider").build();
			context.abortWith(response);
		} 
	}

}

