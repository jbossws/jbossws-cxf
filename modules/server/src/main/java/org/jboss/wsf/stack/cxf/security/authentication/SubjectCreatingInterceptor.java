/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.Collections;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.ws.security.wss4j.AbstractUsernameTokenAuthenticatingInterceptor;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * 
 * @author Sergey Beryozkin
 *
 */
public class SubjectCreatingInterceptor extends AbstractUsernameTokenAuthenticatingInterceptor
{
   private static final Logger log = Logger.getLogger(SubjectCreatingInterceptor.class);
   private SecurityAdaptorFactory secAdaptorFactory;

   public SubjectCreatingInterceptor()
   {
      this(Collections.<String, Object> emptyMap());
   }

   public SubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      secAdaptorFactory = spiProvider.getSPI(SecurityAdaptorFactory.class);
   }

   @Override
   public Subject createSubject(String name, String password, boolean isDigest, String nonce, String created)
   {
      // Load AuthenticationManager
      // TODO : use PicketBox API

      AuthenticationManagerLoader aml = null;
      try
      {
         aml = AuthenticationManagerLoader.class.newInstance();
      }
      catch (Exception ex)
      {
         String msg = "AuthenticationManager can not be loaded";
         log.error(msg);
         throw new SecurityException(msg);
      }

      AuthenticationManager am = aml.getManager();

      // verify timestamp and nonce if digest
      if (isDigest)
      {
         //verifyUsernameToken(nonce, created);
         // CallbackHandler cb = new UsernameTokenCallbackHandler(nonce, created);
         // CallbackHandlerPolicyContextHandler.setCaallbackHandler(cb); 
      }

      // authenticate and populate Subject

      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();

      boolean TRACE = log.isTraceEnabled();
      if (TRACE)
         log.trace("About to authenticate, using security domain '" + am.getSecurityDomain() + "'");

      if (am.isValid(principal, password, subject) == false)
      {
         String msg = "Authentication failed, principal=" + principal.getName();
         log.error(msg);
         throw new SecurityException(msg);
      }

      // push subject on the thread local storage
      SecurityAdaptor adaptor = secAdaptorFactory.newSecurityAdapter();
      adaptor.setPrincipal(principal);
      adaptor.setCredential(password);
      adaptor.pushSubjectContext(subject, principal, password);

      if (TRACE)
         log.trace("Authenticated, principal=" + name);

      return subject;
   }

   /** TODO: JBWS-3028
   private static final int TIMESTAMP_FRESHNESS_THRESHOLD = 300;
   private NonceStore nonceStore;
   
   private void verifyUsernameToken(String nonce, String created)
   {
      if (created != null)
      {
         Calendar cal = SimpleTypeBindings.unmarshalDateTime(created);
         Calendar ref = Calendar.getInstance();
         ref.add(Calendar.SECOND, -TIMESTAMP_FRESHNESS_THRESHOLD);
         if (ref.after(cal))
            throw new SecurityException("Request rejected since a stale timestamp has been provided: " + created);
      }

      if (nonce != null)
      {
         if (nonceStore.hasNonce(nonce))
            throw new SecurityException("Request rejected since a message with the same nonce has been recently received; nonce = " + nonce);
      }
   }
   */
}
