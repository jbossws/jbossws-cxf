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
package org.jboss.wsf.stack.cxf.security.authentication.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.wsf.security.auth.callback.MapCallback;

/**
 * A callback handler to be used to pass parameters to the
 * UsernameTokenCallback.
 * 
 * @author alessio.soldano@jboss.com
 * @since 12-Mar-2008
 *
 */
public class UsernameTokenCallbackHandler implements CallbackHandler
{
   private final String nonce;

   private final String created;

   private final boolean decodeNonce;

   public UsernameTokenCallbackHandler(String nonce, String created, boolean decodeNonce)
   {
      this.created = created;
      this.nonce = nonce;
      this.decodeNonce = decodeNonce;
   }

   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      boolean foundCallback = false;
      Callback firstUnknown = null;
      int count = callbacks != null ? callbacks.length : 0;
      for (int n = 0; n < count; n++)
      {
         Callback c = callbacks[n];
         if (c instanceof MapCallback)
         {
            //set parameters to the MapCallback the UsernameTokenCallback
            //created and set up in the init method
            MapCallback mc = (MapCallback) c;
            mc.setInfo(UsernameTokenCallback.NONCE, nonce);
            mc.setInfo(UsernameTokenCallback.CREATED, created);
            mc.setInfo(UsernameTokenCallback.DECODE_NONCE, Boolean.valueOf(decodeNonce));
            foundCallback = true;
         }
         else if (firstUnknown == null)
         {
            firstUnknown = c;
         }
      }
      if (foundCallback == false)
         throw new UnsupportedCallbackException(firstUnknown, "Unrecognized Callback");
   }

}
