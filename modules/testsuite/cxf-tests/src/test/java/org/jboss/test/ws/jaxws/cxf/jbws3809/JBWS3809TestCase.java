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
package org.jboss.test.ws.jaxws.cxf.jbws3809;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3809] AbstractDeployment#addEndpoint's check on urlPattern performed on wrong data
 * 
 * User: rsearls
 * Date: 7/25/14
 */
@RunWith(Arquillian.class)
public class JBWS3809TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3809.jar");
      archive.addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.BasicEjb.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbPortComponentUri.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebContext.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceNoServicename.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.dups.EjbWebServiceNoServicename.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceServicename.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceDupServicename.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceProvider.class)
         .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3809/META-INF/jboss-webservices.xml")), "META-INF/jboss-webservices.xml")
         .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3809/META-INF/ejb-jar.xml")), "META-INF/ejb-jar.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEjbPortComponentUri() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbPortComponentUriService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/MyEjbPortComponentUri?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbPortComponentUri: confirmed", proxy.getStr("confirmed"));
   }

   @Test
   @RunAsClient
   public void testEjbWebContext() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebContextService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/MyEjbWebContext?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebContext: confirmed", proxy.getStr("confirmed"));
   }

   @Test
   @RunAsClient
   public void testEjbWebServiceNoServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebServiceNoServicenameService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/MyEjbWebServiceNoServicenameService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceNoServicename: confirmed", proxy.getStr("confirmed"));
   }

   @Test
   @RunAsClient
   public void testDupEjbWebServiceNoServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebServiceNoServicenameService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/MyDupEjbWebServiceNoServicenameService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("DupEjbWebServiceNoServicename: confirmed", proxy.getStr("confirmed"));
   }

   @Test
   @RunAsClient
   public void testEjbWebServiceServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "ServicenameEjbWebService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/ServicenameEjbWebService/EjbWebServiceServicename?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceServicename: confirmed", proxy.getStr("confirmed"));
   }

   @Test
   @RunAsClient
   public void testEjbWebServiceDupServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "ServicenameEjbWebService");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/ServicenameEjbWebService/EjbWebServiceDupServicename?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceDupServicename: confirmed", proxy.getStr("confirmed"));
   }


   @Test
   @RunAsClient
   public void testEjbWebServiceProvider() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "MyEjbWebServiceProvider");
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-jbws3809/EjbWebServiceProvider?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceProvider: confirmed", proxy.getStr("confirmed"));
   }

}
