/**
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof;

import org.jboss.wsf.stack.cxf.extensions.security.PasswordCallbackHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * User: rsearls@redhat.com
 * Date: 1/26/14
 */
public class OnBehalfOfCallbackHandler extends PasswordCallbackHandler {

   public OnBehalfOfCallbackHandler()
   {
      super(getInitMap());
   }

   private static Map<String, String> getInitMap()
   {
      Map<String, String> passwords = new HashMap<String, String>();
      passwords.put("myactaskey", "aspass");
      passwords.put("alice", "clarinet");
      passwords.put("bob", "trombone");
      return passwords;
   }

}
