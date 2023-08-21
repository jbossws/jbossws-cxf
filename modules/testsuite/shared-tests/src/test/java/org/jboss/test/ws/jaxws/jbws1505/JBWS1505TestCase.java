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
package org.jboss.test.ws.jaxws.jbws1505;

import java.net.URL;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1505] Verify wsdl generation on SEI inheritance.
 */
@RunWith(Arquillian.class)
public class JBWS1505TestCase extends JBossWSTest
{
   private static Interface2 port;
   private static URL wsdlURL;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1505.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1505.CustomType.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1505.Interface1.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1505.Interface2.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1505.JBWS1505EndpointImpl.class);
      return archive;
   }

   @Before
   public void setup() throws Exception
   {
      if (port == null) {
         QName serviceName = new QName("http://org.jboss.test.ws/jbws1505", "JBWS1505Service");
         wsdlURL = new URL(baseURL + "/jaxws-jbws1505/JBWS1505Service/JBWS1505EndpointImpl?wsdl");
   
         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(Interface2.class);
      }
   }
   
   @AfterClass
   public static void cleanup() throws Exception
   {
      wsdlURL = null;
      port = null;
   }

   /**
    * All methods on the SEI should be mapped.
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testWSDLGeneration() throws Exception
   {
      Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlURL.toString());
      Map<?, ?> services = wsdl.getAllServices();
      assertTrue(services.size() == 1); // a simple port
      javax.wsdl.Service service = (javax.wsdl.Service)services.values().iterator().next();
      javax.wsdl.Port port = (javax.wsdl.Port)service.getPorts().values().iterator().next();
      assertTrue(port.getBinding().getBindingOperations().size() == 5); // with five op's
   }

   /**
    * Complex types that inherit from a SEI hirarchy shold expose
    * all members in xml schema.
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testTypeInheritance() throws Exception
   {
      CustomType ct = port.getCustomType();
      assertTrue(ct.getMember1() == 1);
      assertTrue(ct.getMember2() == 2);
   }
}
