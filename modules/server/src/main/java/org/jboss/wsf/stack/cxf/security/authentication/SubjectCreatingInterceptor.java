/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.AbstractUsernameTokenAuthenticatingInterceptor;
import org.jboss.logging.Logger;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.SecurityDomainContext;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallbackHandler;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;
import org.jboss.xb.binding.SimpleTypeBindings;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * 
 * @author Sergey Beryozkin
 * @author alessio.soldano@jboss.com
 *
 */
public class SubjectCreatingInterceptor extends AbstractUsernameTokenAuthenticatingInterceptor
{
   private static final Logger log = Logger.getLogger(SubjectCreatingInterceptor.class);

   private static final int TIMESTAMP_FRESHNESS_THRESHOLD = 300;

   private boolean propagateContext;

   private int timestampThreshold = TIMESTAMP_FRESHNESS_THRESHOLD;

   private NonceStore nonceStore;

   private boolean decodeNonce = true;
   
   private ThreadLocal<SecurityDomainContext> sdc = new ThreadLocal<SecurityDomainContext>();

   public SubjectCreatingInterceptor()
   {
      this(new HashMap<String, Object>());
   }

   public SubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
   }

   @Override
   public void handleMessage(SoapMessage msg) throws Fault {
      Endpoint ep = msg.getExchange().get(Endpoint.class);
      sdc.set(ep.getSecurityDomainContext());
      try
      {
         super.handleMessage(msg);
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
   public Subject createSubject(String name, String password, boolean isDigest, String nonce, String created)
   {
      if (isDigest)
      {
         verifyUsernameToken(nonce, created);
         // It is not possible at the moment to figure out if the digest has been created 
         // using the original nonce bytes or the bytes of the (Base64)-encoded nonce, some 
         // legacy clients might use the (Base64)-encoded nonce bytes when creating a digest; 
         // lets default to true and assume the nonce has been Base-64 encoded, given that 
         // WSS4J client Base64-decodes the nonce before creating the digest

         CallbackHandler handler = new UsernameTokenCallbackHandler(nonce, created, decodeNonce);
         CallbackHandlerPolicyContextHandler.setCallbackHandler(handler);
      }

      // authenticate and populate Subject
      

      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();

      SecurityDomainContext ctx = sdc.get();
      boolean TRACE = log.isTraceEnabled();
      if (TRACE)
         log.trace("About to authenticate, using security domain '" + ctx.getSecurityDomain() + "'");

      try
      {
         if (ctx.isValid(principal, password, subject) == false)
         {
            String msg = "Authentication failed, principal=" + principal.getName();
            log.error(msg);
            throw new SecurityException(msg);
         }
      }
      finally
      {
         if (isDigest)
         {
            // does not remove the TL entry completely but limits the potential
            // growth to a number of available threads in a container 
            CallbackHandlerPolicyContextHandler.setCallbackHandler(null);
         }
      }

      if (TRACE)
         log.trace("Authenticated, principal=" + name);

      if (propagateContext)
      {
         ctx.pushSubjectContext(subject, principal, password);
         if (TRACE)
            log.trace("Security Context has been propagated");
      }
      return subject;
   }

   private void verifyUsernameToken(String nonce, String created)
   {
      if (created != null)
      {
         Calendar cal = SimpleTypeBindings.unmarshalDateTime(created);
         Calendar ref = Calendar.getInstance();
         ref.add(Calendar.SECOND, -timestampThreshold);
         if (ref.after(cal))
            throw new SecurityException("Request rejected since a stale timestamp has been provided: " + created);
      }

      if (nonce != null && nonceStore != null)
      {
         if (nonceStore.hasNonce(nonce))
            throw new SecurityException(
                  "Request rejected since a message with the same nonce has been recently received; nonce = " + nonce);
         nonceStore.putNonce(nonce);
      }
   }

   public void setPropagateContext(boolean propagateContext)
   {
      this.propagateContext = propagateContext;
   }

   public void setTimestampThreshold(int timestampThreshold)
   {
      this.timestampThreshold = timestampThreshold;
   }

   public void setNonceStore(NonceStore nonceStore)
   {
      this.nonceStore = nonceStore;
   }

   public void setDecodeNonce(boolean decodeNonce)
   {
      this.decodeNonce = decodeNonce;
   }

}
