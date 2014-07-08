/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.webserviceref;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Test @javax.xml.ws.WebServiceref with a custom CXF jaxws:client
 * configuration provided through jbossws-cxf.xml file.
 *
 * @author alessio.soldano@jboss.com
 * @since 19-Nov-2009
 */
public class WebServiceRefServletTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-cxf-webserviceref";

   public static BaseDeployment<?> createClientDeployment() {
      return new JBossWSTestHelper.WarDeployment("jaxws-cxf-webserviceref-servlet-client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.EndpointService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.Handler.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.ServletClient.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF-client/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF-client/web.xml"));
         }
      };
   }

   public static BaseDeployment<?> createServerDeployment() {
      return new JBossWSTestHelper.WarDeployment("jaxws-cxf-webserviceref.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.EndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF/web.xml"));
         }
      };
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WebServiceRefServletTestCase.class, JBossWSTestHelper.writeToFile(createServerDeployment()));
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/wsref", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testServletClient() throws Exception
   {
      final String clientDepName = JBossWSTestHelper.writeToFile(createClientDeployment());
      deploy(clientDepName);
      try
      {
         URL url = new URL(TARGET_ENDPOINT_ADDRESS + "-servlet-client?echo=HelloWorld");
         assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
      }
      finally
      {
         undeploy(clientDepName);
      }
   }
}
