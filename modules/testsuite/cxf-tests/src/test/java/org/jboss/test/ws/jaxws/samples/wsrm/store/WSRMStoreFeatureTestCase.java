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
package org.jboss.test.ws.jaxws.samples.wsrm.store;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
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
   
   @Deployment(testable = false)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsrm-store.war");
      archive.addAsManifestResource(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf.impl\n"), "MANIFEST.MF")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/rmstore-ds.xml"), "rmstore-ds.xml")
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.Endpoint.class).addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.RMStoreFeature.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.store.RMStoreCheckInterceptor.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm-store/WEB-INF/web.xml"));
      JBossWSTestHelper.writeToFile(archive);
      return archive;
   }
   
   @Test
   @RunAsClient
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
