/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.wsrm;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.endpoint.Client;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test the CXF WS-ReliableMessaging
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Dec-2007
 */
@RunWith(Arquillian.class)
public class BasicDocTestCase extends JBossWSTest
{
   @Deployment(name="jaxws-cxf-wsrm-basic-doc", testable = false)
   public static WebArchive createDocServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-wsrm-basic-doc.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicDocEndpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicDocEndpointImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-doc/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-doc/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-wsrm-basic-doc?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testClient() throws Exception
   {
      URL wsdlURL = getResourceURL("jaxws/cxf/wsrm/basic-doc/wsrm-basic-doc.wsdl");
      QName serviceName = new QName("http://org.jboss.ws.jaxws.cxf/wsrm", "RMService");

      Service service = Service.create(wsdlURL, serviceName);
      BasicDocEndpoint port = (BasicDocEndpoint)service.getPort(BasicDocEndpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
      ((Client)port).destroy();
   }
   
   @Override
   protected String getClientJarPaths()
   {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-wsrm-basic-client.jar") {
         {
            archive.addManifest().addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/cxf.xml"), "cxf.xml");
         }
      });
   }
   
}
