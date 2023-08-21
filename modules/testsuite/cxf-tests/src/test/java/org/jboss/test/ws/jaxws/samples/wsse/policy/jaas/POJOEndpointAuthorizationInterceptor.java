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
