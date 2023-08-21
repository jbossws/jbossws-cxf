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
package org.jboss.test.ws.jaxws.cxf.mixtype;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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

@RunWith(Arquillian.class)
public class MixedTypeTestCase extends JBossWSTest
{
   private final String targetNS = "http://org.jboss.ws.jaxws.cxf/mixtype";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-mixtype.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOne.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOneEJB3Impl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointTwoImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/mixtype/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/mixtype/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL wsdlOneURL = new URL(baseURL + "/ServiceOne/EndpointOne?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service service = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)service.getPort(new QName(targetNS, "EndpointOnePort"), EndpointOne.class);
      int initialCount = endpoint.getCount();
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount() - initialCount);
   }
   
   @Test
   @RunAsClient
   public void testEJBEndpoint() throws Exception
   {
      URL wsdlOneURL = new URL(baseURL + "/EJBServiceOne/EndpointOneEJB3Impl?wsdl");
      QName serviceOneName = new QName(targetNS, "EJBServiceOne");
      Service service = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)service.getPort(new QName(targetNS, "EJBEndpointOnePort"), EndpointOne.class);
      int initialCount = endpoint.getCount();
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(5, endpoint.getCount() - initialCount);
   }

   @Test
   @RunAsClient
   public void testEndpoint2() throws Exception
   {
      //verify everything works with an endpoint extending another one impl
      URL wsdlOneURL = new URL(baseURL + "/ServiceOne/EndpointTwo?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service service = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)service.getPort(new QName(targetNS, "EndpointTwoPort"), EndpointOne.class);
      int initialCount = endpoint.getCount();
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount() - initialCount);
   }
   
   @Test
   @RunAsClient
   public void testEndpoint2WithAnotherURLPattern() throws Exception
   {
      //verify everything works with an endpoint extending another one impl
      URL wsdlOneURL = new URL(baseURL + "/ServiceOne/AnotherEndpointTwo?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service service = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)service.getPort(new QName(targetNS, "EndpointTwoPort"), EndpointOne.class);
      int initialCount = endpoint.getCount();
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount() - initialCount);
   }
 
}