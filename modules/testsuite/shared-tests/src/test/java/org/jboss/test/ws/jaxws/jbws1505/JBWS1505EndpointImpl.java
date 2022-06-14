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
package org.jboss.test.ws.jaxws.jbws1505;

import jakarta.jws.WebService;
import jakarta.ejb.Stateless;

/**
 * @author Heiko.Braun@jboss.com
 */
@WebService(
		endpointInterface = "org.jboss.test.ws.jaxws.jbws1505.Interface2",
		serviceName = "JBWS1505Service",
		targetNamespace = "http://org.jboss.test.ws/jbws1505"
)
@Stateless
public class JBWS1505EndpointImpl implements Interface2
{

	private int member = 1;
	private int member2 = 2;

	public int getMember2()
	{
		return this.member2;
	}

	public void setMember2(int member2)
	{
		this.member = member2;
	}

	public CustomType getCustomType()
	{
		return new CustomType();
	}

	public int getMember1()
	{
		return this.member;
	}

	public void setMember1(int member1)
	{
		this.member = member1;
	}
}
