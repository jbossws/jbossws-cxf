/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.configuration;

import java.io.IOException;
import java.security.AccessController;
import java.util.List;
import java.util.Properties;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

import org.apache.cxf.frontend.ServerFactoryBean;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.login.BaseAuthenticationInfo;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.interceptor.JaspiSeverInInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.JaspiSeverOutInterceptor;
import org.jboss.wsf.stack.cxf.jaspi.JaspiServerAuthenticator;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConfigProvider;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConstants;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 */
public class ServerBeanCustomizer extends BeanCustomizer
{
   private WSDLFilePublisher wsdlPublisher;

   private List<Endpoint> depEndpoints;
   
   private UnifiedVirtualFile deploymentRoot;
   
   private String epConfigName;
   
   private String epConfigFile;
   
   @Override
   public void customize(Object beanInstance)
   {
      if (beanInstance instanceof EndpointImpl)
      {
         configureEndpoint((EndpointImpl) beanInstance);
      }
      if (beanInstance instanceof ServerFactoryBean)
      {
         ServerFactoryBean factory = (ServerFactoryBean) beanInstance;

         if (factory.getInvoker() instanceof JBossWSInvoker)
         {
            ((JBossWSInvoker) factory.getInvoker()).setTargetBean(factory.getServiceBean());
         }
         if (depEndpoints != null)
         {
            final String targetBeanName = factory.getServiceBean().getClass().getName();
            for (Endpoint depEndpoint : depEndpoints)
            {
               if (depEndpoint.getTargetBeanClass().getName().equals(targetBeanName))
               {
                  depEndpoint.addAttachment(ServerFactoryBean.class, factory);
               }
            }
         }
      }
      super.customize(beanInstance);
   }

   protected void configureEndpoint(EndpointImpl endpoint)
   {
      //Configure wsdl file publisher
      if (wsdlPublisher != null)
      {
         endpoint.setWsdlPublisher(wsdlPublisher);
      }
      //Configure according to the specified jaxws endpoint configuration
      if (!endpoint.isPublished()) //before publishing, we set the jaxws conf
      {
         Object implementor = endpoint.getImplementor();

         // setup our invoker for http endpoints if invoker is not configured in jbossws-cxf.xml DD
         boolean isHttpEndpoint = endpoint.getAddress() != null && endpoint.getAddress().substring(0, 5).toLowerCase().startsWith("http");
         if ((endpoint.getInvoker() == null) && isHttpEndpoint) 
         {
            endpoint.setInvoker(new JBossWSInvoker());
         }
         
         // ** Endpoint configuration setup **
         // 1) default values
         String configName = org.jboss.wsf.spi.metadata.config.EndpointConfig.STANDARD_ENDPOINT_CONFIG;
         String configFile = null;
         // 2) annotation contribution
         EndpointConfig epConfigAnn = implementor.getClass().getAnnotation(EndpointConfig.class);
         if (epConfigAnn != null)
         {
            if (!epConfigAnn.configName().isEmpty())
            {
               configName = epConfigAnn.configName();
            }
            if (!epConfigAnn.configFile().isEmpty())
            {
               configFile = epConfigAnn.configFile();
            }
         }
         // 3) descriptor overrides (jboss-webservices.xml or web.xml)
         if (epConfigName != null && !epConfigName.isEmpty())
         {
            configName = epConfigName;
         }
         if (epConfigFile != null && !epConfigFile.isEmpty())
         {
            configFile = epConfigFile;
         }
         // 4) setup of configuration
         if (configFile == null)
         {
            //use endpoint configs from AS domain
            ServerConfig sc = getServerConfig();
            org.jboss.wsf.spi.metadata.config.EndpointConfig config = sc.getEndpointConfig(configName);
            if (config != null) {
               endpoint.setEndpointConfig(config);
            } else {
                throw Messages.MESSAGES.couldNotFindEndpointConfigName(configName);
            }
         }
         else
         {
            //look for provided endpoint config file
            try
            {
               UnifiedVirtualFile vf = deploymentRoot.findChild(configFile);
               ConfigRoot config = ConfigMetaDataParser.parse(vf.toURL());
               endpoint.setEndpointConfig(config.getEndpointConfigByName(configName));  
            }
            catch (IOException e)
            {
               throw Messages.MESSAGES.couldNotReadConfigFile(configFile);
            }
         }
         
         if (endpoint.getProperties().get(JaspiServerAuthenticator.JASPI_SECURITY_DOMAIN) != null) {
            String  jaspiSecurityDomain = (String)endpoint.getProperties().get(JaspiServerAuthenticator.JASPI_SECURITY_DOMAIN);
            addJaspiInterceptor(endpoint, jaspiSecurityDomain);    
          } 
         
      }
   }
   
   private static ServerConfig getServerConfig() {
      if(System.getSecurityManager() == null) {
         return AbstractServerConfig.getServerIntegrationServerConfig();
      }
      return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
   }
   
   public void setDeploymentRoot(UnifiedVirtualFile deploymentRoot)
   {
      this.deploymentRoot = deploymentRoot;
   }

   public void setWsdlPublisher(WSDLFilePublisher wsdlPublisher)
   {
      this.wsdlPublisher = wsdlPublisher;
   }

   public void setDeploymentEndpoints(List<Endpoint> endpoints)
   {
      this.depEndpoints = endpoints;
   }
   
   public void setEpConfigName(String epConfigName)
   {
      this.epConfigName = epConfigName;
   }

   public void setEpConfigFile(String epConfigFile)
   {
      this.epConfigFile = epConfigFile;
   }
   
   
   private void addJaspiInterceptor(EndpointImpl endpoint, String securityDomain) {
      if (securityDomain == null) {
         return;
      }
      ApplicationPolicy appPolicy = SecurityConfiguration.getApplicationPolicy(securityDomain);
      if (appPolicy == null) {
         Loggers.ROOT_LOGGER.noApplicationPolicy(securityDomain);
         return;
      }
      BaseAuthenticationInfo bai = appPolicy.getAuthenticationInfo();
      if (bai == null || bai instanceof AuthenticationInfo) {
         Loggers.ROOT_LOGGER.noJaspiApplicationPolicy(securityDomain);
         return;
      } 
      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo) bai;
      String contextRoot = depEndpoints.get(0).getService().getContextRoot();
      String appId = "localhost " + contextRoot;
      AuthConfigFactory factory = AuthConfigFactory.getFactory();
      Properties properties = new Properties();
      AuthConfigProvider provider = new JBossWSAuthConfigProvider(properties, factory);
      provider = factory.getConfigProvider(JBossWSAuthConstants.SOAP_LAYER, appId, null);

      JBossCallbackHandler callbackHandler = new JBossCallbackHandler();
      JaspiServerAuthenticator serverAuthenticator = null;
      try
      {
         ServerAuthConfig serverConfig = provider.getServerAuthConfig(JBossWSAuthConstants.SOAP_LAYER, appId, callbackHandler);
         Properties serverContextProperties = new Properties();
         serverContextProperties.put("security-domain", securityDomain);
         serverContextProperties.put("jaspi-policy", jai);
         serverContextProperties.put(javax.xml.ws.Endpoint.class, endpoint);
         String authContextID = endpoint.getBeanName();
         ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, null, serverContextProperties);
         serverAuthenticator = new JaspiServerAuthenticator(sctx);
         endpoint.getInInterceptors().add(new JaspiSeverInInterceptor(serverAuthenticator));
         endpoint.getOutInterceptors().add(new JaspiSeverOutInterceptor(serverAuthenticator));
      }
      catch (Exception e)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotCreateServerAuthContext(securityDomain);
      }     
   }

}
