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

import org.apache.cxf.frontend.ServerFactoryBean;
import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.security.authentication.AutenticationMgrSubjectCreatingInterceptor;

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
         //JASPI
         final JASPIAuthenticationProvider jaspiProvider = (JASPIAuthenticationProvider) ServiceLoader.loadService(
               JASPIAuthenticationProvider.class.getName(), null, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
         if (jaspiProvider == null)
         {
            Loggers.DEPLOYMENT_LOGGER.cannotFindJaspiClasses();
         }
         else
         {
            if (jaspiProvider.enableServerAuthentication(endpoint, depEndpoints.get(0)))
            {
               endpoint.getInInterceptors().add(new AutenticationMgrSubjectCreatingInterceptor());
            }
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
   
   

}
