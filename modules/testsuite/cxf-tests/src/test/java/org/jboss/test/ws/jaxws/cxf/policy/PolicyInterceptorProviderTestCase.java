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
package org.jboss.test.ws.jaxws.cxf.policy;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.IgnorablePolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * @author alessio.soldano@jboss.com
 * @since 16-Oct-2012
 */
public class PolicyInterceptorProviderTestCase extends JBossWSTest
{
   private String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-policy/PIPService/PIPEndpoint";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-cxf-policy.jar") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf.impl\n")) //cxf impl required due to custom interceptor using cxf-rt-ws-policy in deployment
               .addClass(org.jboss.test.ws.jaxws.cxf.policy.PIPEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.policy.PolicyInterceptorProviderInstallerInterceptor.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/policy/META-INF/unknown-policy.xml"), "unknown-policy.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(PolicyInterceptorProviderTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   /**
    * Verifies the policy-enabled client can be configured to ignore a given policy in the wsdl contract
    * 
    * @throws Exception
    */
   public void testUnsupportedPolicy() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         PolicyInterceptorProviderRegistry reg = bus.getExtension(PolicyInterceptorProviderRegistry.class);
         reg.register(new IgnorablePolicyInterceptorProvider(new QName("http://my.custom.org/policy", "MyPolicy")));
         
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
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
   
   public void testUnsupportedPolicyFail() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
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
