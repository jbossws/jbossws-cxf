/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
