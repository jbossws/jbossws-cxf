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
package org.jboss.wsf.stack.cxf.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.cxf.annotations.UseAsyncMethod;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.configuration.BasicConfigResolver;
import org.jboss.ws.common.deployment.DefaultHttpEndpoint;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.AnnotationsInfo;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.security.authentication.AuthenticationMgrSubjectCreatingInterceptor;
import org.jboss.wsf.stack.cxf.transport.JBossWSDestinationRegistryImpl;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 */
public class ServerBeanCustomizer extends BeanCustomizer
{
   private WSDLFilePublisher wsdlPublisher;

   private ArchiveDeployment dep;

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
         List<Endpoint> depEndpoints = dep.getService().getEndpoints();
         if (depEndpoints != null)
         {
            final String targetBeanName = factory.getServiceBean().getClass().getName();
            for (Endpoint depEndpoint : depEndpoints)
            {
               if (depEndpoint.getTargetBeanClass().getName().equals(targetBeanName))
               {
                  depEndpoint.addAttachment(Object.class, factory.getServiceBean());
               }
            }
         }
      }
      if (beanInstance instanceof ServiceImpl) {
         ServiceImpl service = (ServiceImpl) beanInstance;
         List<Endpoint> depEndpoints = dep.getService().getEndpoints();
         if (depEndpoints != null)
         {
            final Collection<org.apache.cxf.endpoint.Endpoint> eps = service.getEndpoints().values();
            for (Endpoint depEp : depEndpoints) {
               for (org.apache.cxf.endpoint.Endpoint ep : eps) {
                  if (ep.getService().getName().equals(depEp.getProperty(Message.WSDL_SERVICE)) && ep.getEndpointInfo().getName().equals(depEp.getProperty(Message.WSDL_PORT))
                          && ep.getEndpointInfo().getAddress().equals(depEp.getAddress()) ) {
                     depEp.addAttachment(org.apache.cxf.endpoint.Endpoint.class, ep);
                  }
               }
            }
         }
      }
      if (beanInstance instanceof HTTPTransportFactory) {
         HTTPTransportFactory factory = (HTTPTransportFactory) beanInstance;
         DestinationRegistry oldRegistry = factory.getRegistry();
         if (!(oldRegistry instanceof JBossWSDestinationRegistryImpl)) {
            factory.setRegistry(new JBossWSDestinationRegistryImpl());
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
         final Object implementor = endpoint.getImplementor();

         // setup our invoker for http endpoints if invoker is not configured in jbossws-cxf.xml DD
         boolean isHttpEndpoint = endpoint.getAddress() != null && endpoint.getAddress().substring(0, 5).toLowerCase(Locale.ENGLISH).startsWith("http");
         if ((endpoint.getInvoker() == null) && isHttpEndpoint)
         {
            final AnnotationsInfo ai = dep.getAttachment(AnnotationsInfo.class);
            endpoint.setInvoker(new JBossWSInvoker(ai.hasAnnotatedClasses(UseAsyncMethod.class.getName())));
         }
         
         // ** Endpoint configuration setup **
         final String endpointClassName = implementor.getClass().getName();
         final List<Endpoint> depEndpoints = dep.getService().getEndpoints();
         for (Endpoint depEndpoint : depEndpoints) {
            if (endpointClassName.equals(depEndpoint.getTargetBeanName())) {
               org.jboss.wsf.spi.metadata.config.EndpointConfig config = depEndpoint.getEndpointConfig();
               if (config == null) {
                  //the ASIL did not set the endpoint configuration, perhaps because we're processing an
                  //Endpoint.publish() API started endpoint or because we're on WildFly 8.0.0.Final or
                  //previous version. We compute the config here then (clearly no container injection
                  //will be performed on optional handlers attached to the config)
                  BasicConfigResolver bcr = new BasicConfigResolver(dep, implementor.getClass());
                  config = bcr.resolveEndpointConfig();
                  depEndpoint.setEndpointConfig(config);
               }
               if (config != null) {
                  endpoint.setEndpointConfig(config);
               }
               
               //also save Service QName and Port QName in the endpoint for later matches
               depEndpoint.setProperty(Message.WSDL_PORT, endpoint.getEndpointName());
               depEndpoint.setProperty(Message.WSDL_SERVICE, endpoint.getServiceName());
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
               endpoint.getInInterceptors().add(new AuthenticationMgrSubjectCreatingInterceptor());
            }
         }
      }
   }

   public void setWsdlPublisher(WSDLFilePublisher wsdlPublisher)
   {
      this.wsdlPublisher = wsdlPublisher;
   }

   public void setDeployment(ArchiveDeployment dep)
   {
      this.dep = dep;
   }
}
