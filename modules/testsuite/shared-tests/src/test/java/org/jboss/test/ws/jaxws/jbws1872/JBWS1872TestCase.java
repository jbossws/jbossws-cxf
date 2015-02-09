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
package org.jboss.test.ws.jaxws.jbws1872;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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
