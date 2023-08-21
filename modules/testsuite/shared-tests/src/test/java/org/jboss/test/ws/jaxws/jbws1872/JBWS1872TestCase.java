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
package org.jboss.test.ws.jaxws.jbws1872;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1872] EJB3 WebService implementation must have @Remote (instead of @Local) Business interface
 *
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1872TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1872.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1872.EJB3Bean1.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1872.EJB3Bean2.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1872.EJB3Bean3.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1872.LocalIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1872.RemoteIface.class);
      return archive;
   }

  @Test
  @RunAsClient
   public void testEJB1() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1872/Bean1?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean1Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client1 port = service.getPort(Client1.class);
      String retStr = port.echo("hello");
      assertEquals("bean1-hello", retStr);
   }

   @Test
   @RunAsClient
   public void testEJB2() throws Exception
   {
     URL wsdlURL = new URL(baseURL + "/jaxws-jbws1872/Bean2?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean2Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client2 port = service.getPort(Client2.class);
      String retStr = port.echo("hello");
      assertEquals("bean2-hello", retStr);
   }

   @Test
   @RunAsClient
   public void testEJB3() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1872/Bean3?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean3Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client3 port = service.getPort(Client3.class);
      String retStr = port.echo("hello");
      assertEquals("bean3-hello", retStr);
   }

}
