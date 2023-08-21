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
package org.jboss.wsf.stack.cxf.security.authentication;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.TokenType;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.DefaultSecurityContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.wss4j.UsernameTokenInterceptor;
import org.apache.wss4j.common.principal.UsernameTokenPrincipal;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.SecurityDomainContext;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.wsf.stack.cxf.i18n.Messages;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * To be used for policy-first scenarios
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-May-2011
 */
public class SubjectCreatingPolicyInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static final Class<?> groupClass;

   static
   {
      Class<?> clazz = null;
      try
      {
         clazz = Class.forName("java.security.acl.Group");
      } catch (Throwable t)
      {
         // ignore
      }
      groupClass = clazz;
   }

   protected final SubjectCreator helper = new SubjectCreator();

   public SubjectCreatingPolicyInterceptor()
   {
      this(Phase.PRE_PROTOCOL);
      addAfter(UsernameTokenInterceptor.class.getName());
   }
   
   public SubjectCreatingPolicyInterceptor(String phase)
   {
      super(phase);
      helper.setPropagateContext(true);
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      Endpoint ep = message.getExchange().get(Endpoint.class);
      SecurityDomainContext sdc = ep.getSecurityDomainContext();
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
         subject = createSubject(sdc, ut.getName(), ut.getPassword(), ut.isHashed(), ut.getNonce(), ut.getCreatedTime());

      }
      else
      {
         //Try authenticating using WSS4J internal info (previously set into SecurityContext by WSS4JInInterceptor)
         Principal p = context.getUserPrincipal();
         if (!(p instanceof UsernameTokenPrincipal)) {
            throw Messages.MESSAGES.couldNotGetSubjectInfo();
         }
         UsernameTokenPrincipal up = (UsernameTokenPrincipal) p;
         subject = createSubject(sdc, up.getName(), up.getPassword(), up.isPasswordDigest(), up.getNonce(), up.getCreatedTime());
      }

      Principal principal = getPrincipal(context.getUserPrincipal(), subject);
      message.put(SecurityContext.class, createSecurityContext(principal, subject));
   }
   
   protected Subject createSubject(SecurityDomainContext sdc, String name, String password, boolean isDigest, String nonce, String creationTime)
   {
      Subject subject = null;
      try
      {
         subject = helper.createSubject(sdc, name, password, isDigest, nonce, creationTime);
      }
      catch (Exception ex)
      {
         throw Messages.MESSAGES.authenticationFailedSubjectNotCreated(ex);
      }
      if (subject == null || subject.getPrincipals().size() == 0)
      {
         throw Messages.MESSAGES.authenticationFailedSubjectInvalid();
      }
      return subject;
   }

   protected Subject createSubject(SecurityDomainContext sdc, String name, String password, boolean isDigest, byte[] nonce, String creationTime)
   {
      Subject subject = null;
      try
      {
         subject = helper.createSubject(sdc, name, password, isDigest, nonce, creationTime);
      }
      catch (Exception ex)
      {
         throw Messages.MESSAGES.authenticationFailedSubjectNotCreated(ex);
      }
      if (subject == null || subject.getPrincipals().size() == 0)
      {
         throw Messages.MESSAGES.authenticationFailedSubjectInvalid();
      }
      return subject;
   }

   protected Principal getPrincipal(Principal originalPrincipal, Subject subject)
   {
      Set<Principal> principals = subject.getPrincipals();
      if (!principals.isEmpty())
      {
          Principal principal = principals.iterator().next();
          if (groupClass == null || !groupClass.isInstance(principal))
          {
              return principal;
          }
      }
      return originalPrincipal;
   }

   protected SecurityContext createSecurityContext(Principal p, Subject subject)
   {
      return new DefaultSecurityContext(p, subject);
   }

   public void setPropagateContext(boolean propagateContext)
   {
      this.helper.setPropagateContext(propagateContext);
   }

   public void setTimestampThreshold(int timestampThreshold)
   {
      this.helper.setTimestampThreshold(timestampThreshold);
   }

   public void setNonceStore(NonceStore nonceStore)
   {
      this.helper.setNonceStore(nonceStore);
   }

   public void setDecodeNonce(boolean decodeNonce)
   {
      this.helper.setDecodeNonce(decodeNonce);
   }

}
