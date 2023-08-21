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
package org.jboss.wsf.stack.cxf.extensions.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class PasswordCallbackHandler implements CallbackHandler
{

   private Map<String, String> passwords = new HashMap<String, String>();

   public PasswordCallbackHandler(Map<String, String> initMap)
   {
      passwords.putAll(initMap);
   }

   /**
    * It attempts to get the password from the private 
    * alias/passwords map.
    */
   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         final Callback c = callbacks[i];
         if (c != null && c instanceof WSPasswordCallback) {
            final WSPasswordCallback pc = (WSPasswordCallback)c;
   
            String pass = passwords.get(pc.getIdentifier());
            if (pass != null)
            {
               pc.setPassword(pass);
               return;
            }
         }
      }
   }

   /**
    * Add an alias/password pair to the callback mechanism.
    */
   public void setAliasPassword(String alias, String password)
   {
      passwords.put(alias, password);
   }
}
