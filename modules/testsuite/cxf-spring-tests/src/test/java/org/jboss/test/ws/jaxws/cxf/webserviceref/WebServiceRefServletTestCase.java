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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

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

   public static Test suite()
   {
      return new JBossWSTestSetup(WebServiceRefServletTestCase.class, "jaxws-cxf-webserviceref.war");
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
      deploy("jaxws-cxf-webserviceref-servlet-client.war");
      try
      {
         URL url = new URL(TARGET_ENDPOINT_ADDRESS + "-servlet-client?echo=HelloWorld");
         BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
         String retStr = br.readLine();
         assertEquals("HelloWorld", retStr);
      }
      finally
      {
         undeploy("jaxws-cxf-webserviceref-servlet-client.war");
      }
   }
}
