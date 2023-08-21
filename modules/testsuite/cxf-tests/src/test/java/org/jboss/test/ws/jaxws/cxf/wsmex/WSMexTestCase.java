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
package org.jboss.test.ws.jaxws.cxf.wsmex;

import java.net.URL;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.mex.MetadataExchange;
import org.apache.cxf.ws.mex.model._2004_09.Metadata;
import org.apache.cxf.ws.mex.model._2004_09.MetadataSection;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMWriter;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

/**
 * Test WS-MetadataExchange
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-May-2012
 */
@RunWith(Arquillian.class)
public class WSMexTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-wsmex.jar");
      archive.addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.wsmex.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.wsmex.EndpointBean.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      JaxWsProxyFactoryBean proxyFac = new JaxWsProxyFactoryBean();
      proxyFac.setAddress(baseURL + "/jaxws-cxf-wsmex/EndpointService");
      MetadataExchange exc = proxyFac.create(MetadataExchange.class);
      Metadata metadata = exc.get2004();

      assertNotNull(metadata);
      assertEquals(1, metadata.getMetadataSection().size());

      MetadataSection ms = metadata.getMetadataSection().get(0);
      assertEquals("http://schemas.xmlsoap.org/wsdl/", ms.getDialect());
      assertEquals("http://org.jboss.ws/cxf/wsmex", ms.getIdentifier());

      String wsdl = DOMWriter.printNode((Node)ms.getAny(), true);
      assertTrue(wsdl.contains("EndpointBeanServiceSoapBinding"));
   }
}
