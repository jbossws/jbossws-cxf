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
package org.jboss.test.ws.jaxws.cxf.policy;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.IgnorablePolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author alessio.soldano@jboss.com
 * @since 16-Oct-2012
 */
@RunWith(Arquillian.class)
public class PolicyInterceptorProviderTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-policy.jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.apache.cxf.impl\n")) //cxf impl required due to custom interceptor using cxf-rt-ws-policy in deployment
         .addClass(org.jboss.test.ws.jaxws.cxf.policy.PIPEndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.policy.PolicyInterceptorProviderInstallerInterceptor.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/policy/META-INF/unknown-policy.xml"), "unknown-policy.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/policy/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   /**
    * Verifies the policy-enabled client can be configured to ignore a given policy in the wsdl contract
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testUnsupportedPolicy() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         PolicyInterceptorProviderRegistry reg = bus.getExtension(PolicyInterceptorProviderRegistry.class);
         reg.register(new IgnorablePolicyInterceptorProvider(new QName("http://my.custom.org/policy", "MyPolicy")));
         
         URL wsdlURL = new URL(baseURL + "/jaxws-cxf-policy/PIPService/PIPEndpoint?wsdl");
         QName serviceName = new QName("http://policy.cxf.jaxws.ws.test.jboss.org/", "PIPService");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         QName portQName = new QName("http://policy.cxf.jaxws.ws.test.jboss.org/", "PIPEndpointPort");
         PIPEndpoint port = (PIPEndpoint)service.getPort(portQName, PIPEndpoint.class);

         assertEquals("foo", port.echo("foo"));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testUnsupportedPolicyFail() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         URL wsdlURL = new URL(baseURL + "/jaxws-cxf-policy/PIPService/PIPEndpoint?wsdl");
         QName serviceName = new QName("http://policy.cxf.jaxws.ws.test.jboss.org/", "PIPService");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         QName portQName = new QName("http://policy.cxf.jaxws.ws.test.jboss.org/", "PIPEndpointPort");
         PIPEndpoint port = (PIPEndpoint)service.getPort(portQName, PIPEndpoint.class);

         try {
            port.echo("foo");
            fail("Exception expected");
         } catch (Exception e) {
            assertTrue(e.getMessage().contains("None of the policy alternatives can be satisfied"));
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
}
