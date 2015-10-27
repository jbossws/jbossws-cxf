/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.DefaultSecurityContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.validate.UsernameTokenValidator;
import org.apache.wss4j.dom.validate.Validator;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.SecurityDomainContext;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * 
 * @author Sergey Beryozkin
 * @author alessio.soldano@jboss.com
 *
 */
public class SubjectCreatingInterceptor extends WSS4JInInterceptor
{
   protected final SubjectCreator helper = new SubjectCreator();
  
   private static final Logger LOG = LogUtils.getL7dLogger(SubjectCreatingInterceptor.class);
   
   private final ThreadLocal<SecurityDomainContext> sdc = new ThreadLocal<SecurityDomainContext>();
  
   private boolean supportDigestPasswords;

   public SubjectCreatingInterceptor()
   {
      this(new HashMap<String, Object>());
   }

   public SubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
      getAfter().add(PolicyBasedWSS4JInInterceptor.class.getName());
   }

   public void setSupportDigestPasswords(boolean support)
   {
      supportDigestPasswords = support;
   }

   public boolean getSupportDigestPasswords()
   {
      return supportDigestPasswords;
   }

   @Override
   public void handleMessage(SoapMessage msg) throws Fault {
      Endpoint ep = msg.getExchange().get(Endpoint.class);
      sdc.set(ep.getSecurityDomainContext());
      try
      {
         SecurityToken token = msg.get(SecurityToken.class);
         SecurityContext context = msg.get(SecurityContext.class);
         if (token == null || context == null || context.getUserPrincipal() == null) {
             super.handleMessage(msg);
             return;
         }
         UsernameToken ut = (UsernameToken)token;
         
         Subject subject = createSubject(ut.getName(), ut.getPassword(), ut.isHashed(),
                                         ut.getNonce(), ut.getCreatedTime());
         
         SecurityContext sc = doCreateSecurityContext(context.getUserPrincipal(), subject);
         msg.put(SecurityContext.class, sc);
      }
      finally
      {
         if (sdc != null)
         {
            sdc.remove();
         }
      }
   }
   
   @Override
   protected SecurityContext createSecurityContext(final Principal p) {
       Message msg = PhaseInterceptorChain.getCurrentMessage();
       if (msg == null) {
           throw new IllegalStateException("Current message is not available");
       }
       return doCreateSecurityContext(p, msg.get(Subject.class));
   }
   
   /**
    * Creates default SecurityContext which implements isUserInRole using the
    * following approach : skip the first Subject principal, and then check optional
    * Groups the principal is a member of. Subclasses can override this method and implement
    * a custom strategy instead
    *   
    * @param p principal
    * @param subject subject 
    * @return security context
    */
   protected SecurityContext doCreateSecurityContext(final Principal p, final Subject subject) {
       return new DefaultSecurityContext(p, subject);
   }
   
   protected void setSubject(String name, String password, boolean isDigest, String nonce, String created)
         throws WSSecurityException
   {
      Message msg = PhaseInterceptorChain.getCurrentMessage();
      if (msg == null)
      {
         throw new IllegalStateException("Current message is not available");
      }
      Subject subject = null;
      try
      {
         subject = createSubject(name, password, isDigest, nonce, created);
      }
      catch (Exception ex)
      {
         String errorMessage = "Failed Authentication : Subject has not been created";
         LOG.severe(errorMessage);
         throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
      }
      if (subject == null || subject.getPrincipals().size() == 0 || !checkUserPrincipal(subject.getPrincipals(), name))
      {
         String errorMessage = "Failed Authentication : Invalid Subject";
         LOG.severe(errorMessage);
         throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
      }
      msg.put(Subject.class, subject);
   }
   
   private boolean checkUserPrincipal(Set<Principal> principals, String name)
   {
      for (Principal p : principals) {
         if (!(p instanceof Group)) {
            return p.getName().equals(name);
         }
      }
      return false;
   }

   @Override 
   protected WSSecurityEngine getSecurityEngine(boolean utNoCallbacks) {
       Map<QName, Object> profiles = new HashMap<QName, Object>(1);
       
       Validator validator = new CustomValidator();
       profiles.put(WSConstants.USERNAME_TOKEN, validator);
       return createSecurityEngine(profiles);
   }
   
   protected class CustomValidator extends UsernameTokenValidator {
       
       @Override
       protected void verifyCustomPassword(
           org.apache.wss4j.dom.message.token.UsernameToken usernameToken,
           RequestData data
       ) throws WSSecurityException {
          SubjectCreatingInterceptor.this.setSubject(
               usernameToken.getName(), usernameToken.getPassword(), false, null, null
           );
       }
       
       @Override
       protected void verifyPlaintextPassword(
           org.apache.wss4j.dom.message.token.UsernameToken usernameToken,
           RequestData data
       ) throws WSSecurityException {
          SubjectCreatingInterceptor.this.setSubject(
               usernameToken.getName(), usernameToken.getPassword(), false, null, null
           );
       }
       
       @Override
       protected void verifyDigestPassword(
           org.apache.wss4j.dom.message.token.UsernameToken usernameToken,
           RequestData data
       ) throws WSSecurityException {
           if (!supportDigestPasswords) {
               throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
           }
           String user = usernameToken.getName();
           String password = usernameToken.getPassword();
           boolean isHashed = usernameToken.isHashed();
           String nonce = usernameToken.getNonce();
           String createdTime = usernameToken.getCreated();
           SubjectCreatingInterceptor.this.setSubject(
               user, password, isHashed, nonce, createdTime
           );
       }
       
       @Override
       protected void verifyUnknownPassword(
           org.apache.wss4j.dom.message.token.UsernameToken usernameToken,
           RequestData data
       ) throws WSSecurityException {
          SubjectCreatingInterceptor.this.setSubject(
               usernameToken.getName(), null, false, null, null
           );
       }
       
   }

   public Subject createSubject(String name, String password, boolean isDigest, String nonce, String created)
   {
      return helper.createSubject(sdc.get(), name, password, isDigest, nonce, created);
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
