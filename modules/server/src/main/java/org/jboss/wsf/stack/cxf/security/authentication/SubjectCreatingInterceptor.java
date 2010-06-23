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

import java.io.IOException;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.ws.security.wss4j.AbstractUsernameTokenAuthenticatingInterceptor;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallbackHandler;
import org.jboss.wsf.stack.cxf.security.nonce.NonceStore;
import org.jboss.xb.binding.SimpleTypeBindings;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * 
 * @author Sergey Beryozkin
 *
 */
public class SubjectCreatingInterceptor extends AbstractUsernameTokenAuthenticatingInterceptor
{
   private static final Logger log = Logger.getLogger(SubjectCreatingInterceptor.class);
   private static final int TIMESTAMP_FRESHNESS_THRESHOLD = 300;
      
   private AuthenticationManagerLoader aml;
   private boolean propagateContext;
   private SecurityAdaptorFactory secAdaptorFactory;
   private int timestampThreshold = TIMESTAMP_FRESHNESS_THRESHOLD;
   private NonceStore nonceStore; 
   private boolean decodeNonce = true;

   private boolean supportDigestPasswords;
   
   public SubjectCreatingInterceptor()
   {
      this(Collections.<String, Object> emptyMap());
   }

   public SubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
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
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      secAdaptorFactory = spiProvider.getSPI(SecurityAdaptorFactory.class);

   }

   // TODO : this code is a temporarily workaround; AbstractUsernameTokenAuthenticatingInterceptor
   // has a bug to do with handling digests; RequestData assumes PasswordDigest by default
   @Override
   public void setSupportDigestPasswords(boolean support) {
	   this.supportDigestPasswords = support;
	   super.setSupportDigestPasswords(support);
   }
   
   // TODO : this code is a temporarily workaround; AbstractUsernameTokenAuthenticatingInterceptor
   // has a bug to do with handling digests; RequestData assumes PasswordDigest by default 
   @Override
   protected CallbackHandler getCallback(RequestData reqData, int doAction) 
       throws WSSecurityException {
       
       if (supportDigestPasswords) {    
           return new CallbackHandler() 
           {
			 @Override
			 public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException 
			 {
				// dummy handler
			 }	       	   
           };
       } else {
           return super.getCallback(reqData, doAction);
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
      AuthenticationManager am = aml.getManager();
      
      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();

      boolean TRACE = log.isTraceEnabled();
      if (TRACE)
         log.trace("About to authenticate, using security domain '" + am.getSecurityDomain() + "'");

      try 
      {
	      if (am.isValid(principal, password, subject) == false)
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
    	  SecurityAdaptor adaptor = secAdaptorFactory.newSecurityAdapter();
          adaptor.pushSubjectContext(subject, principal, password);
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
            throw new SecurityException("Request rejected since a message with the same nonce has been recently received; nonce = " + nonce);
         nonceStore.putNonce(nonce);
      }
   }

   public void setPropagateContext(boolean propagateContext) {
       this.propagateContext = propagateContext;
   }
   
   public void setTimestampThreshold(int timestampThreshold) {
   	  this.timestampThreshold = timestampThreshold;
   }

   public void setNonceStore(NonceStore nonceStore) {
	  this.nonceStore = nonceStore;
   }

   public void setDecodeNonce(boolean decodeNonce) {
	  this.decodeNonce = decodeNonce;
   }

}
