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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

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
