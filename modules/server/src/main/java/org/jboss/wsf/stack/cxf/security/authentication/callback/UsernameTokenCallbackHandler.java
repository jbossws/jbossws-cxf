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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.security.auth.callback.MapCallback;

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
   private String nonce;
   private String created;
   private boolean decodeNonce;
   
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
      for(int n = 0; n < count; n ++)
      {
         Callback c = callbacks[n];
         if( c instanceof MapCallback )
         {
            //set parameters to the MapCallback the UsernameTokenCallback
            //created and set up in the init method
            MapCallback mc = (MapCallback) c;
            mc.setInfo(UsernameTokenCallback.NONCE, nonce);
            mc.setInfo(UsernameTokenCallback.CREATED, created);
            mc.setInfo(UsernameTokenCallback.DECODE_NONCE, Boolean.valueOf(decodeNonce));
            foundCallback = true;
         }
         else if( firstUnknown == null )
         {
            firstUnknown = c;
         }
      }
      if( foundCallback == false )
         throw new UnsupportedCallbackException(firstUnknown, "Unrecognized Callback");         
   }

}
