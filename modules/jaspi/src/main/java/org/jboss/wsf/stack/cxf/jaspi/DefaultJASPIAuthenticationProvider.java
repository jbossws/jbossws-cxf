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
package org.jboss.wsf.stack.cxf.jaspi;

import java.util.Map;
import java.util.Properties;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws22.EndpointImpl;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.login.BaseAuthenticationInfo;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.jaspi.client.JaspiClientAuthenticator;
import org.jboss.wsf.stack.cxf.jaspi.client.JaspiClientInInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.client.JaspiClientOutInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConfigProvider;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConstants;
import org.jboss.wsf.stack.cxf.jaspi.interceptor.JaspiSeverInInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.interceptor.JaspiSeverOutInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.log.Loggers;

/**
 * Class to enable the jaspi authentication interceptors in cxf bus , endpoint or client
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class DefaultJASPIAuthenticationProvider implements JASPIAuthenticationProvider
{
   public DefaultJASPIAuthenticationProvider()
   {
   }

   public boolean enableClientAuthentication(Object target, Map<String, String> properties)
   {
      if (!(target instanceof Client)) {
         Loggers.ROOT_LOGGER.cannotEnableJASPIAuthentication(target.getClass().getSimpleName());
         return false;
      }
      Client client = (Client)target;
      String securityDomain = properties.get(JaspiClientAuthenticator.JASPI_SECURITY_DOMAIN);
      if (securityDomain == null)
      {
         return false;
      }
      ApplicationPolicy appPolicy = SecurityConfiguration.getApplicationPolicy(securityDomain);
      if (appPolicy == null)
      {
         Loggers.ROOT_LOGGER.noApplicationPolicy(securityDomain);
         return false;
      }
      BaseAuthenticationInfo bai = appPolicy.getAuthenticationInfo();
      if (bai == null || bai instanceof AuthenticationInfo)
      {
         Loggers.ROOT_LOGGER.noJaspiApplicationPolicy(securityDomain);
         return false;
      }
      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo) bai;

      String contextRoot = client.getEndpoint().getEndpointInfo().getName().toString();
      String appId = "localhost " + contextRoot;
      AuthConfigFactory factory = AuthConfigFactory.getFactory();

      Properties props = new Properties();
      AuthConfigProvider provider = new JBossWSAuthConfigProvider(props, factory);
      provider = factory.getConfigProvider(JBossWSAuthConstants.SOAP_LAYER, appId, null);
      JBossCallbackHandler callbackHandler = new JBossCallbackHandler();
      try
      {
         ClientAuthConfig clientConfig = provider.getClientAuthConfig("soap", appId, callbackHandler);
         JaspiClientAuthenticator clientAuthenticator = new JaspiClientAuthenticator(clientConfig, securityDomain, jai);
         client.getInInterceptors().add(new JaspiClientInInterceptor(clientAuthenticator));
         client.getOutInterceptors().add(new JaspiClientOutInterceptor(clientAuthenticator));
      }
      catch (Exception e)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotCreateServerAuthContext(securityDomain, e);
      }

      return false;

   }
   
   public boolean enableServerAuthentication(Deployment dep, JBossWebservicesMetaData wsmd)
   {
      String securityDomain = null;
      if (wsmd != null)
      {
         securityDomain = wsmd.getProperty(JaspiServerAuthenticator.JASPI_SECURITY_DOMAIN);
      }
      if (securityDomain == null)
      {
         return false;
      }
      ApplicationPolicy appPolicy = SecurityConfiguration.getApplicationPolicy(securityDomain);
      if (appPolicy == null)
      {
         Loggers.ROOT_LOGGER.noApplicationPolicy(securityDomain);
         return false;
      }
      BaseAuthenticationInfo bai = appPolicy.getAuthenticationInfo();
      if (bai == null || bai instanceof AuthenticationInfo)
      {
         Loggers.ROOT_LOGGER.noJaspiApplicationPolicy(securityDomain);
         return false;
      }
      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo) bai;

      String contextRoot = dep.getService().getContextRoot();
      String appId = "localhost " + contextRoot;
      AuthConfigFactory factory = AuthConfigFactory.getFactory();
      Properties properties = new Properties();
      AuthConfigProvider provider = new JBossWSAuthConfigProvider(properties, factory);
      provider = factory.getConfigProvider(JBossWSAuthConstants.SOAP_LAYER, appId, null);

      JBossCallbackHandler callbackHandler = new JBossCallbackHandler();
      try
      {
         ServerAuthConfig serverConfig = provider.getServerAuthConfig(JBossWSAuthConstants.SOAP_LAYER, appId,
               callbackHandler);
         Properties serverContextProperties = new Properties();
         serverContextProperties.put("security-domain", securityDomain);
         serverContextProperties.put("jaspi-policy", jai);
         Bus bus = dep.getAttachment(Bus.class);
         serverContextProperties.put(Bus.class, bus);
         String authContextID = dep.getSimpleName();
         ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, null, serverContextProperties);
         JaspiServerAuthenticator serverAuthenticator = new JaspiServerAuthenticator(sctx);
         bus.getInInterceptors().add(new JaspiSeverInInterceptor(serverAuthenticator));
         bus.getOutInterceptors().add(new JaspiSeverOutInterceptor(serverAuthenticator));
         return true;
      }
      catch (Exception e)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotCreateServerAuthContext(securityDomain, e);
      }
      return false;
   }

   public boolean enableServerAuthentication(Object target, Endpoint endpoint)
   {
      if (!(target instanceof EndpointImpl)) {
         Loggers.ROOT_LOGGER.cannotEnableJASPIAuthentication(target.getClass().getSimpleName());
         return false;
      }
      EndpointImpl endpointImpl = (EndpointImpl)target;
      String securityDomain = (String) endpointImpl.getProperties().get(JaspiServerAuthenticator.JASPI_SECURITY_DOMAIN);
      if (securityDomain == null)
      {
         return false;
      }
      ApplicationPolicy appPolicy = SecurityConfiguration.getApplicationPolicy(securityDomain);
      if (appPolicy == null)
      {
         Loggers.ROOT_LOGGER.noApplicationPolicy(securityDomain);
         return false;
      }
      BaseAuthenticationInfo bai = appPolicy.getAuthenticationInfo();
      if (bai == null || bai instanceof AuthenticationInfo)
      {
         Loggers.ROOT_LOGGER.noJaspiApplicationPolicy(securityDomain);
         return false;
      }
      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo) bai;
      String contextRoot = endpoint.getService().getContextRoot();
      String appId = "localhost " + contextRoot;
      AuthConfigFactory factory = AuthConfigFactory.getFactory();
      Properties properties = new Properties();
      AuthConfigProvider provider = new JBossWSAuthConfigProvider(properties, factory);
      provider = factory.getConfigProvider(JBossWSAuthConstants.SOAP_LAYER, appId, null);

      JBossCallbackHandler callbackHandler = new JBossCallbackHandler();
      JaspiServerAuthenticator serverAuthenticator = null;
      try
      {
         ServerAuthConfig serverConfig = provider.getServerAuthConfig(JBossWSAuthConstants.SOAP_LAYER, appId,
               callbackHandler);
         Properties serverContextProperties = new Properties();
         serverContextProperties.put("security-domain", securityDomain);
         serverContextProperties.put("jaspi-policy", jai);
         serverContextProperties.put(javax.xml.ws.Endpoint.class, endpointImpl);
         String authContextID = endpointImpl.getBeanName();
         ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, null, serverContextProperties);
         serverAuthenticator = new JaspiServerAuthenticator(sctx);
         endpointImpl.getInInterceptors().add(new JaspiSeverInInterceptor(serverAuthenticator));
         endpointImpl.getOutInterceptors().add(new JaspiSeverOutInterceptor(serverAuthenticator));
         return true;
        
      }
      catch (Exception e)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotCreateServerAuthContext(securityDomain, e);
      }
      return false;
   }
}
