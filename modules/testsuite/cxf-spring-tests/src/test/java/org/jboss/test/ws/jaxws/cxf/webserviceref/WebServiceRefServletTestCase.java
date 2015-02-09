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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployer;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;


/**
 * Test @javax.xml.ws.WebServiceref with a custom CXF jaxws:client
 * configuration provided through jbossws-cxf.xml file.
 *
 * @author alessio.soldano@jboss.com
 * @since 19-Nov-2009
 */
@RunWith(Arquillian.class)
public class WebServiceRefServletTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-webserviceref";

   @ArquillianResource
   private Deployer deployer;

   private static final String CLIENT_WAR ="jaxws-cxf-webserviceref-servlet-client";
   @Deployment(name=CLIENT_WAR, testable = false, managed=false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-cxf-webserviceref-servlet-client.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.EndpointService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.Handler.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.ServletClient.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF-client/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF-client/web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-cxf-webserviceref", testable = false)
   public static WebArchive createServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-cxf-webserviceref.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.webserviceref.EndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/webserviceref/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-cxf-webserviceref")
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

   @Test
   @RunAsClient
   public void testServletClient() throws Exception
   {
      deployer.deploy(CLIENT_WAR);
      try
      {
         URL url = new URL(TARGET_ENDPOINT_ADDRESS + "-servlet-client?echo=HelloWorld");
         assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
      }
      finally
      {
         deployer.undeploy(CLIENT_WAR);
      }
   }
}
