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
package org.jboss.test.ws.jaxws.misc;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Misc tests that require a simple ws endpoint deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2014
 */
public class MiscTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-misc/endpoint";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-misc.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.misc.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.misc.EndpointImpl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(MiscTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testEndpoint() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/misc", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   /**
    * [JBWS-3741] WebService doesn't support "//"
    * 
    */
   public void testJBWS3741() throws Exception
   {
      assertTrue(IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":8080//jaxws-misc/endpoint?wsdl").openStream()).contains("wsdl:definitions"));
      assertTrue(IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":8080/jaxws-misc///endpoint?wsdl").openStream()).contains("wsdl:definitions"));
   }

   /**
    * [JBWS-3743] Block HTTP GET requests with no query string
    * 
    */
   public void testJBWS3743() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS);
      final HttpURLConnection c = (HttpURLConnection)wsdlURL.openConnection();
      c.connect();
      assertEquals(405, c.getResponseCode());
      String error = IOUtils.readAndCloseStream(c.getErrorStream());
      c.disconnect();
      assertEquals("HTTP GET not supported", error);
   }
}
