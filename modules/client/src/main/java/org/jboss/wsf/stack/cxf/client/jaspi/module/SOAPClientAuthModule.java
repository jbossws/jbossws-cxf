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
package org.jboss.wsf.stack.cxf.client.jaspi.module;

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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.ws.security.WSSConfig;
import org.jboss.security.SimplePrincipal;

/** 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class SOAPClientAuthModule implements ClientAuthModule
{

   @SuppressWarnings("rawtypes")
   private List<Class> supportedTypes = new ArrayList<Class>();
   private SimplePrincipal principal = null;
   private Object credential = null;

   @SuppressWarnings("unused")
   private MessagePolicy requestPolicy = null;
   @SuppressWarnings("unused")
   private MessagePolicy responsePolicy = null;
   @SuppressWarnings("unused")
   private CallbackHandler handler = null;
   @SuppressWarnings({ "rawtypes" })
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
   public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException
   {
      this.requestPolicy = requestPolicy;
      this.responsePolicy = responsePolicy;
      this.handler = handler;
      this.options = options;
   }

   @SuppressWarnings({ "unchecked" })
   public AuthStatus secureRequest(MessageInfo messageInfo, Subject source) throws AuthException
   {

      SOAPMessage soapMessage = (SOAPMessage)messageInfo.getRequestMessage();
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
      subject.getPrincipals().remove(principal);
      subject.getPublicCredentials().remove(credential);
   }

}
