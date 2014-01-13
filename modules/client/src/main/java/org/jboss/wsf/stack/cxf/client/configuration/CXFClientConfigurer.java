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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.login.BaseAuthenticationInfo;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.client.jaspi.JaspiClientAuthenticator;
import org.jboss.wsf.stack.cxf.client.jaspi.JaspiClientInInterceptor;
import org.jboss.wsf.stack.cxf.client.jaspi.JaspiClientOutInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConfigProvider;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConstants;

/**
 * CXF extension of common ClientConfigurer
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Jul-2012
 *
 */
public class CXFClientConfigurer extends ConfigHelper
{
   private static final String JBOSSWS_CXF_CLIENT_CONF_PROPS = "jbossws.cxf.client.conf.props";
   
   @Override
   public void setConfigProperties(Object client, String configFile, String configName) {
      ClientConfig config = readConfig(configFile, configName);
      Client cxfClient;
      if (client instanceof DispatchImpl<?>) {
         cxfClient = ((DispatchImpl<?>)client).getClient();
      } else {
         cxfClient = ClientProxy.getClient(client);
      }
      cleanupPreviousProps(cxfClient);
      Map<String, String> props = config.getProperties();
      if (props != null && !props.isEmpty()) {
         savePropList(cxfClient, props);
      }
      setConfigProperties(cxfClient, props);
      
      //config jaspi
      JaspiClientAuthenticator clientAuthenticator = getJaspiAuthenticator(cxfClient, props) ;
      if (clientAuthenticator != null) {
         cxfClient.getInInterceptors().add(new JaspiClientInInterceptor(clientAuthenticator));
         cxfClient.getOutInterceptors().add(new JaspiClientOutInterceptor(clientAuthenticator));
      }
      
   }
   
   
   private JaspiClientAuthenticator getJaspiAuthenticator(Client client, Map<String, String> properties) {
      String securityDomain = properties.get(JaspiClientAuthenticator.JASPI_SECURITY_DOMAIN);
      if (securityDomain == null) {
         return null;            
      }
      ApplicationPolicy appPolicy = SecurityConfiguration.getApplicationPolicy(securityDomain);
      if (appPolicy == null) {
         Loggers.ROOT_LOGGER.noApplicationPolicy(securityDomain);
         return null;
      }
      BaseAuthenticationInfo bai = appPolicy.getAuthenticationInfo();
      if (bai == null || bai instanceof AuthenticationInfo) {
         Loggers.ROOT_LOGGER.noJaspiApplicationPolicy(securityDomain);
         return null;
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
         return new JaspiClientAuthenticator(clientConfig, securityDomain, jai);
      }
      catch (Exception e)
      {
         //ignore
      }
      
      return null;
      
   }   
   
   public void setConfigProperties(Client client, Map<String, String> properties) {
      client.getEndpoint().putAll(properties);
   }
   
   private void savePropList(Client client, Map<String, String> props) {
      final Set<String> keys = props.keySet();
      client.getEndpoint().put(JBOSSWS_CXF_CLIENT_CONF_PROPS, (String[])keys.toArray(new String[keys.size()]));
   }
   
   private void cleanupPreviousProps(Client client) {
      Endpoint ep = client.getEndpoint();
      String[] previousProps = (String[])ep.get(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      if (previousProps != null) {
         for (String p : previousProps) {
            ep.remove(p);
         }
         ep.remove(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      }
   }
}
