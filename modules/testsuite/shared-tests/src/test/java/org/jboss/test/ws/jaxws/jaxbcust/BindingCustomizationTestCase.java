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
package org.jboss.test.ws.jaxws.jaxbcust;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.api.binding.JAXBBindingCustomization;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 *
 * @since 28-Jun-2007
 */
@RunWith(Arquillian.class)
public class BindingCustomizationTestCase {

   @Test
   @RunAsClient
   @SuppressWarnings("unchecked")
   public void testCustomizationWriteAccess() throws Exception
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      DeploymentModelFactory deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
      
      Endpoint endpoint = deploymentModelFactory.newHttpEndpoint(null);
      BindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put("com.sun.xml.bind.defaultNamespaceRemap", "http://org.jboss.bindingCustomization");
      endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);

      // a started endpoint should deny customizations
      try
      {
         endpoint.setState(EndpointState.STARTED);
         endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);

         fail("It should not be possible to change bindinig customizations on a started endpoint");
      }
      catch (Exception e)
      {
         // all fine, this should happen
      }
   }


   @Test
   @RunAsClient
   @SuppressWarnings("unchecked")
   public void testCustomizationReadAccess() throws Exception
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      DeploymentModelFactory deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
      
      Endpoint endpoint = deploymentModelFactory.newHttpEndpoint(null);
      BindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put("com.sun.xml.bind.defaultNamespaceRemap", "http://org.jboss.bindingCustomization");
      endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);
      endpoint.setState(EndpointState.STARTED);

      // read a single customization
      BindingCustomization knownCustomization = endpoint.getAttachment(BindingCustomization.class);
      assertNotNull(knownCustomization);

      // however the iteratoion should be unmodifiable
      try
      {
         endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);
         fail("Started Endpoints should only expose read acccess to their binding customizations");
      }
      catch (Exception e)
      {
         // all fine, we'd expect this
      }


   }
}
