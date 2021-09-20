/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.jaspi.client.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ClientAuthModule;
import jakarta.xml.soap.SOAPMessage;

/**
 * SOAPClientAuthModule
 * TODO: Investigate what we can do with this module
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class SOAPClientAuthModule implements ClientAuthModule
{
   public static String log;

   @SuppressWarnings("rawtypes")
   private List<Class> supportedTypes = new ArrayList<Class>();

   @SuppressWarnings("unused")
   private MessagePolicy requestPolicy = null;

   @SuppressWarnings("unused")
   private MessagePolicy responsePolicy = null;

   @SuppressWarnings("unused")
   private CallbackHandler handler = null;

   @SuppressWarnings(
   {"rawtypes", "unused"})
   private Map options = null;

   public SOAPClientAuthModule()
   {
      this.supportedTypes.add(Object.class);
      this.supportedTypes.add(SOAPMessage.class);
   }

   @SuppressWarnings("rawtypes")
   public SOAPClientAuthModule(List<Class> supportedTypes)
   {
      this.supportedTypes = supportedTypes;
   }

   @SuppressWarnings("rawtypes")
   public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
         Map options) throws AuthException
   {
      this.requestPolicy = requestPolicy;
      this.responsePolicy = responsePolicy;
      this.handler = handler;
      this.options = options;
   }

   public AuthStatus secureRequest(MessageInfo messageInfo, Subject source) throws AuthException
   {
      log = "secureRequest";
      return AuthStatus.SUCCESS;
   }

   public AuthStatus validateResponse(MessageInfo messageInfo, Subject source, Subject recipient) throws AuthException
   {
      return AuthStatus.SUCCESS;
   }

   @SuppressWarnings("rawtypes")
   public Class[] getSupportedMessageTypes()
   {
      Class[] clsarr = new Class[this.supportedTypes.size()];
      supportedTypes.toArray(clsarr);
      return clsarr;
   }

   public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException
   {
      //TODO: implement this if secureRequest or valdiateResponse is required
   }

}
