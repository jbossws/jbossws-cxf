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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.ws.addressing.Names;
import org.apache.cxf.ws.addressing.policy.AddressingAssertionBuilder;
import org.apache.cxf.ws.addressing.policy.AddressingPolicyInterceptorProvider;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.AssertionBuilderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.PolicyEngineImpl;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyProvider;
import org.apache.cxf.ws.policy.attachment.ServiceModelPolicyProvider;
import org.apache.cxf.ws.policy.attachment.external.DomainExpressionBuilderRegistry;
import org.apache.cxf.ws.policy.attachment.wsdl11.Wsdl11AttachmentPolicyProvider;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertionBuilder;
import org.apache.cxf.ws.policy.mtom.MTOMAssertionBuilder;
import org.apache.cxf.ws.policy.mtom.MTOMPolicyInterceptorProvider;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.policy.RMAssertionBuilder;
import org.apache.cxf.ws.rm.policy.RMPolicyInterceptorProvider;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class JBossWSNonSpringBusFactory extends CXFBusFactory
{
   @SuppressWarnings("rawtypes")
   @Override
   public Bus createBus(Map<Class, Object> extensions, Map<String, Object> properties) {
      if (extensions == null)
      {
         extensions = new HashMap<Class, Object>();
      }
      if (!extensions.containsKey(Configurer.class))
      {
         extensions.put(Configurer.class, new JBossWSNonSpringConfigurer(new BeanCustomizer()));
      }
      
      preparePolicyEngine(extensions);
      
      Bus bus = new ExtensionManagerBus(extensions, properties);

      initPolicyEngine((PolicyEngineImpl)extensions.get(PolicyEngine.class), bus);
      
      possiblySetDefaultBus(bus);
      initializeBus(bus);
      return bus;
   }
   
   @SuppressWarnings("rawtypes")
   private static void preparePolicyEngine(Map<Class, Object> extensions)
   {
      PolicyEngineImpl engine = new PolicyEngineImpl(true);
      extensions.put(PolicyEngine.class, engine);
      DomainExpressionBuilderRegistry domainExpBuilderRegistry = new DomainExpressionBuilderRegistry();
      extensions.put(DomainExpressionBuilderRegistry.class, domainExpBuilderRegistry);
   }
   
   private static void initPolicyEngine(PolicyEngineImpl engine, Bus bus)
   {
      engine.setBus(bus);
      AssertionBuilderRegistry assertionBuilderRegistry = new AssertionBuilderRegistryImpl(bus);
      PolicyInterceptorProviderRegistry policyInterceptorProviderRegistry = new PolicyInterceptorProviderRegistryImpl(bus);
      PolicyBuilderImpl policyBuilder = new PolicyBuilderImpl();
      policyBuilder.setBus(bus);
      policyBuilder.setAssertionBuilderRegistry(assertionBuilderRegistry);
      bus.setExtension(policyBuilder, PolicyBuilder.class);
      Collection<PolicyProvider> policyProviders = engine.getPolicyProviders();
      Wsdl11AttachmentPolicyProvider wsdl11PolicyAttachmentProvider = new Wsdl11AttachmentPolicyProvider(bus);
      wsdl11PolicyAttachmentProvider.setBuilder(policyBuilder);
      wsdl11PolicyAttachmentProvider.setRegistry(engine.getRegistry());
      policyProviders.add(wsdl11PolicyAttachmentProvider);
      ServiceModelPolicyProvider serviceModelPolicyProvider = new ServiceModelPolicyProvider(bus);
      serviceModelPolicyProvider.setBuilder(policyBuilder);
      serviceModelPolicyProvider.setRegistry(engine.getRegistry());
      policyProviders.add(serviceModelPolicyProvider);
      
      //MTOM Policy
      assertionBuilderRegistry.register(new MTOMAssertionBuilder());
      policyInterceptorProviderRegistry.register(new MTOMPolicyInterceptorProvider());
      
      //RM
      RMManager rmManager = new RMManager();
      rmManager.init(bus);
      
      //RM Policy
      policyInterceptorProviderRegistry.register(new RMPolicyInterceptorProvider(bus));
      try
      {
         assertionBuilderRegistry.register(new RMAssertionBuilder());
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
      
      //Addressing Policy
      policyInterceptorProviderRegistry.register(new AddressingPolicyInterceptorProvider());
      assertionBuilderRegistry.register(new AddressingAssertionBuilder(bus));
      Collection<QName> addressingKnownEls = new LinkedList<QName>();
      addressingKnownEls.add(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing/policy", Names.WSAW_USING_ADDRESSING_NAME));
      addressingKnownEls.add(new QName(Names.WSA_NAMESPACE_WSDL_NAME_OLD, Names.WSAW_USING_ADDRESSING_NAME));
      addressingKnownEls.add(Names.WSAW_USING_ADDRESSING_QNAME);
      PrimitiveAssertionBuilder primitiveAssertionBuilder = new PrimitiveAssertionBuilder(addressingKnownEls);
      primitiveAssertionBuilder.setBus(bus);
      assertionBuilderRegistry.register(primitiveAssertionBuilder);
   }
   
   @Override
   protected void initializeBus(Bus bus) {
      super.initializeBus(bus);
   }
}
