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
package org.jboss.wsf.stack.cxf.security.authentication.callback;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Map;

import javax.security.auth.callback.Callback;

import org.jboss.crypto.digest.DigestCallback;
import org.jboss.security.auth.callback.MapCallback;
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
