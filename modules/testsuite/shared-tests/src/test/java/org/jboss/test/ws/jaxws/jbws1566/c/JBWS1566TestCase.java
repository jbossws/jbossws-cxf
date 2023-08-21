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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;
import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1566] Invalid wsdl using @XmlSchema annotations on Types
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1566
 * 
 */
@RunWith(Arquillian.class)
public class JBWS1566TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1566.jar");
         archive
               .addManifest()
               .addPackage("org.jboss.test.ws.jaxws.jbws1566.a")
               .addPackage("org.jboss.test.ws.jaxws.jbws1566.b")
               .addClass(org.jboss.test.ws.jaxws.jbws1566.c.JBWS1566TestCase.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1566.c.Jaxb20StatelessTestBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1566.c.Jaxb20TestWSInterface.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testWebService() throws Exception
   {
      String TARGET_ENDPOINT_ADDRESS = baseURL.toString() + "/jaxwstest/Jaxb20StatelessTestBean";
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      System.out.println("wsdl URL:" + wsdlURL);

      QName serviceName = new QName("http://org.jboss.ws/samples/c", "WebServiceTestService");
      Service service = Service.create(wsdlURL, serviceName);
      Jaxb20TestWSInterface port = service.getPort(Jaxb20TestWSInterface.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      Map<String, Object> reqContext = bindingProvider.getRequestContext();
      reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, TARGET_ENDPOINT_ADDRESS);

      TestEnumeration res = null;
      BClass input = new BClass();
      input.setA(1);
      input.setB("hello service");
      try
      {
         res = port.testMethod(input);
         assertEquals(res, TestEnumeration.A);
      }
      catch (BException e)
      {
         fail("Caught unexpeced TestException: " + e);
      }
      catch (RemoteException e)
      {
         fail("Caught unexpeced RemoteException: " + e);
      }
      assertNotNull(res);
   }
}
