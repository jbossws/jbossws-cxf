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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jboss.logging.Logger;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
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

   private static final Logger LOG = Logger.getLogger(SubjectCreatingInterceptor.class);

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
         LOG.error(errorMessage);
         throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
      }
      if (subject == null || subject.getPrincipals().size() == 0 || !checkUserPrincipal(subject.getPrincipals(), name))
      {
         String errorMessage = "Failed Authentication : Invalid Subject";
         LOG.error(errorMessage);
         throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
      }
      msg.put(Subject.class, subject);
   }

   private boolean checkUserPrincipal(Set<Principal> principals, String name)
   {
      for (Principal p : principals) {
         if (groupClass == null || !groupClass.isInstance(p)) {
            return p.getName().equals(name);
         }
      }
      return false;
   }

   protected WSSecurityEngine getSecurityEngine(boolean utWithCallbacks)
   {
      WSSecurityEngine engine = super.getSecurityEngine(utWithCallbacks);
      if (engine != null)
      {
         engine.getWssConfig().setValidator(WSConstants.USERNAME_TOKEN, new CustomValidator());
      }
      return engine;
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
