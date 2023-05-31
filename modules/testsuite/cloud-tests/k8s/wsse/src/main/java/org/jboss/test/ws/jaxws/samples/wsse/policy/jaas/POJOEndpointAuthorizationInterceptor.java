/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.interceptor.security.SimpleAuthorizingInterceptor;


/**
 * A custom interceptor for method-level POJO endpoint authorization 
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-May-2011
 *
 */
public class POJOEndpointAuthorizationInterceptor extends SimpleAuthorizingInterceptor
{
   
   public POJOEndpointAuthorizationInterceptor()
   {
      super();
      readRoles();
   }
   
   private void readRoles()
   {
      //just an example, this might read from a configuration file or such
      Map<String, String> roles = new HashMap<String, String>();
      roles.put("sayHello", "friend");
      roles.put("greetMe", "snoppies");
      setMethodRolesMap(roles);
   }
}
