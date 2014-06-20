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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.samples.eardeployment.EarTestCase.EarTestCaseDeploymentArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the wsdl is published to local filesystem; this test assumes
 * client and server share the filesystem.
 * 
 * @author alessio.soldano@jboss.com
 */
public class WSDLPublishTestCase extends JBossWSTest
{
   private File wsdlFileDir;
   private static long testStart;
   
   public static Test suite()
   {
      testStart = System.currentTimeMillis();
      return new JBossWSTestSetup(WSDLPublishTestCase.class, EarTestCaseDeploymentArchive.NAME);
   }

   public void testEJB3Endpoint() throws Exception
   {
      String soapAddress = "http://" + getServerHost() + ":8080/earejb3/EndpointService/Endpoint";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      
      File file = new File(getWsdlFileDir().getAbsolutePath() + File.separator + "jaxws-samples-eardeployment.ear" + File.separator
            + "jaxws-samples-eardeployment-ejb3.jar" + File.separator + "Endpoint.wsdl");
      
      assertTrue("Wsdl file not found", file.exists());
      assertTrue("Stale wsdl file found", file.lastModified() > testStart - 1000);
      
      URL wsdlUrl = file.toURI().toURL();
      
      Service service = Service.create(wsdlUrl, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testJSEEndpoint() throws Exception
   {
      String soapAddress = "http://" + getServerHost() + ":8080/earjse/JSEBean";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      
      File file = new File(getWsdlFileDir().getAbsolutePath() + File.separator + "jaxws-samples-eardeployment.ear" + File.separator
            + "jaxws-samples-eardeployment-pojo.war" + File.separator + "Endpoint.wsdl");
      
      assertTrue("Wsdl file not found", file.exists());
      assertTrue("Stale wsdl file found", file.lastModified() > testStart - 1000);
      
      URL wsdlUrl = file.toURI().toURL();

      Service service = Service.create(wsdlUrl, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   private File getWsdlFileDir() throws IOException
   {
      if (wsdlFileDir == null)
      {
         URL url = new URL("http://" + getServerHost() + ":8080/earjse/support");
         wsdlFileDir = new File(IOUtils.readAndCloseStream(url.openStream()), "wsdl");
      }
      return wsdlFileDir;
   }
}
