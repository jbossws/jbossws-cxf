/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import java.security.acl.Group;

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
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.SecurityDomainContext;
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
   private static Logger LOG = Logger.getLogger(SubjectCreatingPolicyInterceptor.class);

   private SubjectCreator helper = new SubjectCreator();

   public SubjectCreatingPolicyInterceptor()
   {
      super(Phase.PRE_INVOKE);
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
         LOG.error("User Principal is not available on the current message"); //TODO i18n
         return;
      }

      SecurityToken token = message.get(SecurityToken.class);
      Subject subject = null;
      if (token != null)
      {
         //Try authenticating using SecurityToken info
         if (token.getTokenType() != TokenType.UsernameToken)
         {
            reportSecurityException("Unsupported token type " + token.getTokenType().toString());
         }
         UsernameToken ut = (UsernameToken) token;
         subject = createSubject(sdc, ut.getName(), ut.getPassword(), ut.isHashed(), ut.getNonce(), ut.getCreatedTime());

      }
      else
      {
         //Try authenticating using WSS4J internal info (previously set into SecurityContext by WSS4JInInterceptor)
         Principal p = context.getUserPrincipal();
         if (!(p instanceof WSUsernameTokenPrincipal)) {
            reportSecurityException("Could not get subject info neither from Security Token in the current message nor directly from computed SecurityContext");
         }
         WSUsernameTokenPrincipal up = (WSUsernameTokenPrincipal) p;
         subject = createSubject(sdc, up.getName(), up.getPassword(), up.isPasswordDigest(), up.getNonce(), up.getCreatedTime());
      }

      Principal principal = getPrincipal(context.getUserPrincipal(), subject);
      message.put(SecurityContext.class, createSecurityContext(principal, subject));
   }

   private Subject createSubject(SecurityDomainContext sdc, String name, String password, boolean isDigest, String nonce, String creationTime)
   {
      Subject subject = null;
      try
      {
         subject = helper.createSubject(sdc, name, password, isDigest, nonce, creationTime);
      }
      catch (Exception ex)
      {
         reportSecurityException("Failed Authentication : Subject has not been created, " + ex.getMessage());
      }
      if (subject == null || subject.getPrincipals().size() == 0)
      {
         reportSecurityException("Failed Authentication : Invalid Subject");
      }
      return subject;
   }

   protected Principal getPrincipal(Principal originalPrincipal, Subject subject)
   {
      Principal[] ps = subject.getPrincipals().toArray(new Principal[]
      {});
      if (ps != null && ps.length > 0 && !(ps[0] instanceof Group))
      {
         return ps[0];
      }
      else
      {
         return originalPrincipal;
      }
   }

   protected SecurityContext createSecurityContext(Principal p, Subject subject)
   {
      return new DefaultSecurityContext(p, subject);
   }

   protected void reportSecurityException(String errorMessage)
   {
      LOG.error(errorMessage); //TODO i18n
      throw new SecurityException(errorMessage);
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
