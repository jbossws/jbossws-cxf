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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Map;

import javax.security.auth.callback.Callback;

import org.jboss.wsf.crypto.digest.DigestCallback;
import org.jboss.wsf.security.auth.callback.MapCallback;
import java.util.Base64;

/**
 * An implementation of DigestCallback that generates password
 * digests according to the UsernameTokenProfile 1.0 specification.
 *
 * @author alessio.soldano@jboss.com
 * @since 12-Mar-2008
 *
 */
public class UsernameTokenCallback implements DigestCallback
{
   static final String NONCE = "nonce";

   static final String CREATED = "created";

   static final String DECODE_NONCE = "decodeNonce";

   private MapCallback info;

   public void init(Map<String,Object> options)
   {
      // Ask for MapCallback to obtain the digest parameters
      info = new MapCallback();
      Callback[] callbacks =
      {info};
      options.put("callbacks", callbacks);
   }

   public void preDigest(MessageDigest digest)
   {
      try
      {
         String nonce = (String) info.getInfo(NONCE);
         if (nonce != null)
         {
            Boolean decodeNonce = (Boolean) info.getInfo(DECODE_NONCE);
            byte[] nonceBytes = decodeNonce ? Base64.getDecoder().decode(nonce) : nonce.getBytes("UTF-8");
            digest.update(nonceBytes);
         }
         String created = (String) info.getInfo(CREATED);
         if (created != null)
            digest.update(created.getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         throw new SecurityException(e);
      }
   }

   public void postDigest(MessageDigest digest)
   {
   }
}
