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
package org.jboss.test.ws.jaxws.cxf.clientcluster;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.clustering.FailoverFeature;
import org.apache.cxf.clustering.RandomStrategy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to demonstrate cxf cluster feature
 *
 *@author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
@RunWith(Arquillian.class)
public class CXFClientClusterTestCase extends JBossWSTest
{
   private static final String DEP1 = "jaxws-cxf-cluster";

   @ArquillianResource
   private URL baseURL;

   @Deployment(name = DEP1, testable = false)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP1 + ".war");
      archive.addManifest().addClass(org.jboss.test.ws.jaxws.cxf.clientcluster.Endpoint.class).addClass(org.jboss.test.ws.jaxws.cxf.clientcluster.EndpointImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientcluster/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCluster() throws Exception
   {
      List<String> serviceList = new ArrayList<String>();
      serviceList.add(baseURL.toExternalForm() + "/ClusetrService");
      RandomStrategy strategy = new RandomStrategy();
      strategy.setAlternateAddresses(serviceList);

      FailoverFeature ff = new FailoverFeature();
      ff.setStrategy(strategy);

      JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
      factory.setAddress("http://localhost:8080/notExist/NotExistPort");
      factory.getFeatures().add(ff);
      factory.setServiceClass(Endpoint.class);
      Endpoint proxy = factory.create(Endpoint.class);
      assertEquals("Unexpected resposne", "cluster", proxy.echo("cluster"));

      URL wsdlURL = new URL(baseURL.toExternalForm() + "/ClusetrService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/cxf/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint endpoint = service.getPort(Endpoint.class, ff);
      ((BindingProvider)endpoint).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/notExist/NotExistPort");
      assertEquals("Unexpected resposne", "cluster", endpoint.echo("cluster"));
   }
}
