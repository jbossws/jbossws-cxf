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
package org.jboss.wsf.stack.cxf.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.cxf.annotations.UseAsyncMethod;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.configuration.BasicConfigResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.AnnotationsInfo;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.wsf.stack.cxf.interceptor.JBossWSSOAPHandlerInterceptor;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingPolicyInterceptor;
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

      if (beanInstance instanceof JaxWsEndpointImpl)
      {
         JaxWsEndpointImpl jaxwsEndpoint = (JaxWsEndpointImpl)beanInstance;
         if (jaxwsEndpoint.getInInterceptors().removeIf(item ->item instanceof SOAPHandlerInterceptor)) {
            jaxwsEndpoint.getInInterceptors().add(new JBossWSSOAPHandlerInterceptor(jaxwsEndpoint.getJaxwsBinding()));
         }

         if(jaxwsEndpoint.getOutInterceptors().removeIf(item ->item instanceof SOAPHandlerInterceptor)) {
            jaxwsEndpoint.getOutInterceptors().add(new JBossWSSOAPHandlerInterceptor(jaxwsEndpoint.getJaxwsBinding()));
         }
         jaxwsEndpoint.getInInterceptors().forEach(n -> { if (n instanceof LogicalHandlerInInterceptor) {
            ((LogicalHandlerInInterceptor)n).addAfter(JBossWSSOAPHandlerInterceptor.class.getName());
         }});
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
               endpoint.getInInterceptors().add(new SubjectCreatingPolicyInterceptor());
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
