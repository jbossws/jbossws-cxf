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
package org.jboss.test.ws.jaxws.samples.wsrm.store;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
/**
 * @author <a herf="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class WSRMStoreFeatureTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   @ArquillianResource
   private InitialContext ctx;
   private static final String DEP1 = "DsDeployment";
   private static final String DEP2 = "RMStoreDeployment";
   
   
   @Deployment(name=DEP1, testable = false, order=1)
   public static JavaArchive createDsDeployment()
   {
	  JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "rmstore-ds.jar");
      archive.addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/rmstore-ds.xml"), "rmstore-ds.xml");
      return archive;
   }
   
   
   @Deployment(name=DEP2, testable = false, order=2)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsrm-store.war");
      archive.addAsManifestResource(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.common, org.apache.cxf.impl\n"), "MANIFEST.MF")
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.Endpoint.class).addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.RMStoreFeature.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.RMStoreCheckInterceptor.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/WEB-INF/jboss-deployment-structure.xml"))
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/WEB-INF/web.xml"))
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/WEB-INF/permissions.xml"), "permissions.xml");
      JBossWSTestHelper.writeToFile(archive);
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP2)
   public void test() throws Exception
   {  
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "RMService");
      Service service = Service.create(new URL(baseURL + "?wsdl"), serviceName);
      Endpoint proxy = (Endpoint)service.getPort(Endpoint.class, new RMStoreFeature());
      assertEquals("Hello World! with RMStore", proxy.checkPersistent("Hello World!"));
      //check client RMStore enabled
      assertTrue("RMStore is not enabled and stores data for client side", RMStoreCheckInterceptor.seqSize > 0);
   }

   
}
