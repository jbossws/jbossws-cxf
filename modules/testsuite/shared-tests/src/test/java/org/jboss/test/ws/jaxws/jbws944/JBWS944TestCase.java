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
package org.jboss.test.ws.jaxws.jbws944;

import java.net.URL;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
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
 * EJB3 jmx name is incorrectly derrived
 * 
 * http://jira.jboss.org/jira/browse/JBWS-944
 *
 * @author Thomas.Diesler@jboss.org
 * @author Jason.Greene@jboss.com
 * @since 29-Apr-2005
 */
@RunWith(Arquillian.class)
public class JBWS944TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws944.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws944.EJB3Bean01.class)
               .addClass(org.jboss.test.ws.jaxws.jbws944.EJB3RemoteBusinessInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws944.EJB3RemoteHome.class)
               .addClass(org.jboss.test.ws.jaxws.jbws944.EJB3RemoteInterface.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = null;
      try {
         iniCtx = getServerInitialContext();
         EJB3RemoteBusinessInterface ejb3Remote = (EJB3RemoteBusinessInterface)iniCtx.lookup("jaxws-jbws944//FooBean01!" + EJB3RemoteBusinessInterface.class.getName());

         String helloWorld = "Hello world!";
         Object retObj = ejb3Remote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      } 
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
      }
   }

   // This tests whether the remote proxy also implements
   // the home interface and that it can be narrowed to it.
   @Test
   @RunAsClient
   public void testNarrowedRemoteAccess() throws Exception
   {
      InitialContext iniCtx = null;
      try {
         iniCtx = getServerInitialContext();
         Object obj = iniCtx.lookup("jaxws-jbws944//FooBean01!" + EJB3RemoteHome.class.getName());
         EJB3RemoteHome ejb3Home = (EJB3RemoteHome)PortableRemoteObject.narrow(obj, EJB3RemoteHome.class);
         EJB3RemoteInterface ejb3Remote = ejb3Home.create();

         String helloWorld = "Hello world!";
         Object retObj = ejb3Remote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      } 
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
      }
   }

   @Test
   @RunAsClient
   public void testWebService() throws Exception
   {
      assertWSDLAccess();

      String helloWorld = "Hello world!";
      QName serviceName = new QName("http://org.jboss.ws/jbws944", "EJB3BeanService");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws944/FooBean01?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void assertWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws944/FooBean01?wsdl");
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
