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
package org.jboss.test.ws.jaxws.cxf.aegis;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

@WebService(endpointInterface="org.jboss.test.ws.jaxws.cxf.aegis.AegisGroupQuery")
public class AegisGroupQueryImpl implements AegisGroupQuery
{
  Map<Integer, Member> members = new HashMap<Integer, Member>();
  
  public Map getMembers() {
     Member member1 = new Member();
     member1.setId(1);
     member1.setName("Jane");
     
     Member member2 = new Member();
     member2.setId(2);
     member2.setName("Mike");
     
     members.put(member1.getId(), member1);
     members.put(member2.getId(), member2);
     return members;
  }

}
