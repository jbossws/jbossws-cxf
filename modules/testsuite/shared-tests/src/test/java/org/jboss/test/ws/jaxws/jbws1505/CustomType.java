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
package org.jboss.test.ws.jaxws.jbws1505;

/**
 * @author Heiko.Braun@jboss.com
 */
public final class CustomType implements Interface2
{
	private int member1 = 1;
	private int member2 = 2;

	public int getMember2()
	{
		return this.member2;
	}

	public void setMember2(int member2)
	{

	}

	public CustomType getCustomType()
	{
		return null;
	}

	public int getMember1()
	{
		return this.member1;
	}

	public void setMember1(int member1)
	{
		
	}
}
