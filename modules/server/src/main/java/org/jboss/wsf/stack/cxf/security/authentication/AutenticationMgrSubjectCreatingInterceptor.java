/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.security.authentication;

import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.TokenType;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.jboss.security.plugins.JBossAuthenticationManager;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;

/** 
 * Interceptor to authenticate principal with provided jaspi JBossAuthenticationManager
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class AutenticationMgrSubjectCreatingInterceptor extends SubjectCreatingPolicyInterceptor
{
   
   public AutenticationMgrSubjectCreatingInterceptor() {
      super();        
   }
  

   @Override
   public void handleMessage(Message message) throws Fault
   {
	  JBossAuthenticationManager authenticationManger = message.get(JBossAuthenticationManager.class);
	  if (authenticationManger == null) {
		  return;
	  }
      SecurityContext context = message.get(SecurityContext.class);
      if (context == null || context.getUserPrincipal() == null)
      {
         Loggers.SECURITY_LOGGER.userPrincipalNotAvailableOnCurrentMessage();
         return;
      }

      SecurityToken token = message.get(SecurityToken.class);
      Subject subject = null;
      if (token != null)
      {
         //Try authenticating using SecurityToken info
         if (token.getTokenType() != TokenType.UsernameToken)
         {
            throw Messages.MESSAGES.unsupportedTokenType(token.getTokenType());
         }
         UsernameToken ut = (UsernameToken) token;
         subject = helper.createSubject(authenticationManger, ut.getName(), ut.getPassword(), ut.isHashed(), ut.getNonce(), ut.getCreatedTime(), message);

      }
      else
      {
         //Try authenticating using WSS4J internal info (previously set into SecurityContext by WSS4JInInterceptor)
         Principal p = context.getUserPrincipal();
         if (!(p instanceof WSUsernameTokenPrincipal)) {
            throw Messages.MESSAGES.couldNotGetSubjectInfo();
         }
         WSUsernameTokenPrincipal up = (WSUsernameTokenPrincipal) p;
         subject = helper.createSubject(authenticationManger, up.getName(), up.getPassword(), up.isPasswordDigest(), up.getNonce(), up.getCreatedTime(), message);
      }

      Principal principal = getPrincipal(context.getUserPrincipal(), subject);
      message.put(SecurityContext.class, createSecurityContext(principal, subject));
   }

  
}
