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

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.security.wss4j.AbstractUsernameTokenAuthenticatingInterceptor;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.picketbox.config.PicketBoxConfiguration;
import org.picketbox.exceptions.ConfigurationStreamNullException;
import org.picketbox.factories.SecurityFactory;

/**
 * Interceptor which authenticates a current principal and populates Subject
 * 
 * @author Sergey Beryozkin
 *
 */
public class SubjectCreatingInterceptor extends AbstractUsernameTokenAuthenticatingInterceptor
{
   private static final Logger log = Logger.getLogger(SubjectCreatingInterceptor.class);
   private static final String DEFAULT_SECURITY_DOMAIN_NAME = "JBossWS";
   
   private String securityDomainName = DEFAULT_SECURITY_DOMAIN_NAME;
   private boolean propagateContext;
   
   public SubjectCreatingInterceptor()
   {
      this(Collections.<String, Object> emptyMap());
   }

   public SubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
   }

   @Override
   public Subject createSubject(String name, String password, boolean isDigest, String nonce, String created)
   {
	  //if (isDigest)
	  //{
	      //verifyUsernameToken(nonce, created);
	      // CallbackHandler cb = new UsernameTokenCallbackHandler(nonce, created);
	      // CallbackHandlerPolicyContextHandler.setCaallbackHandler(cb); 
	  //}
	   
      SecurityContext securityContext = getSecurityContext();

      // authenticate and populate Subject
      AuthenticationManager am = securityContext.getAuthenticationManager();
      
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
      
      if (TRACE)
         log.trace("Authenticated, principal=" + name);

      if (propagateContext) 
      {
	      securityContext.getUtil().createSubjectInfo(principal, password, subject);
	      PhaseInterceptorChain.getCurrentMessage().setContent(SecurityContext.class, securityContext);
	      if (TRACE)
	          log.trace("Security Context has been propagated");
      }
      return subject;
   }

   @Override
   public void handleFault(SoapMessage message) {
	   SecurityContext securityContext = message.getContent(SecurityContext.class);
	   if (securityContext != null) {
	       securityContext.setSubjectInfo(null);
	   } 
   }
  
   private SecurityContext getSecurityContext() {
	   SecurityFactory.prepare();
	      
	   try
	   { 
	      return SecurityFactory.establishSecurityContext(securityDomainName);
	   }
	   catch (Exception ex) {
	      throw new SecurityException("Unable to establish Security Context for domain "
	    		                      + securityDomainName, ex);
	   }
	   finally 
	   {
	      SecurityFactory.release();
	   }
   }
   
   /**
    * Loads a custom configuration file, can be used to add the configuration
    * for new domains or override the default ones configured by JBoss AS
    * 
    * Note : loading a custom configuration file may affect other endpoints running
    * in the same container instance. Example, if some other endpoint depends on
    * a default JBossWS security domain and this custom config file overrides JBossWS
    * then the other endpoint may get affected
    *  
    * @param configFilePath location of the custom configuration file
    */
   public void setSecurityConfigFile(String configFilePath) 
   {
      SecurityFactory.prepare();
      try
      { 
    	 PicketBoxConfiguration idtrustConfig = new PicketBoxConfiguration();
         idtrustConfig.load(configFilePath);
      }
      catch (ConfigurationStreamNullException ex) {
         throw new SecurityException("Unable to load the configuration file " + configFilePath);
      } 
      catch (Exception ex) {
         throw new SecurityException("Unable to read the configuration file " + configFilePath, ex);
      }
      finally 
      {
         SecurityFactory.release();
      }
   }

   /**
    * Sets the security domain name. This property has to be set when loading
    * a custom configuration file. It also can be used to override the default
    * security domain name (JBossWS)
    * @param domainName
    */
   public void setSecurityDomainName(String domainName) {
	   securityDomainName = domainName;
   }

   public void setPropagateContext(boolean propagateContext) {
       this.propagateContext = propagateContext;
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
