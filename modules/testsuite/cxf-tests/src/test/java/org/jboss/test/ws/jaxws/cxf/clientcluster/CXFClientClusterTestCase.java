/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.clientcluster;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

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
