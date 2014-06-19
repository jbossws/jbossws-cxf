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
package org.jboss.test.ws.jaxws.jbws1582;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
 * [JBWS-1582] Protect JBossWS Against XML Attacks
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class JBWS1582TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws1582-attacked.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1582.AttackedEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1582.Endpoint.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/wsdl/attack-service.wsdl"), "wsdl/attack-service.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/attack-web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws1582.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1582.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1582.EndpointImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/wsdl/service.wsdl"), "wsdl/service.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   static {
      JBossWSTestHelper.writeToFile(createDeployments());
   }

   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-jbws1582/TestService";
   private String targetNS = "http://jbws1582.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1582TestCase.class, "jaxws-jbws1582.war");
   }

   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   public void testSOAPMessage() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/message.xml");
      assertTrue(response.contains("HTTP/1.1 200 OK") || response.contains("HTTP/1.0 200 OK"));
      assertTrue(response.contains("<return>Hello</return>"));
   }

   public void testSOAPMessageAttack1() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-1.xml");
      assertTrue(response.contains("HTTP/1.1 500") || response.contains("HTTP/1.0 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }

   public void testSOAPMessageAttack2() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-2.xml");
      assertTrue(response.contains("HTTP/1.1 500") || response.contains("HTTP/1.0 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }

   private String getResponse(String requestFile) throws Exception
   {
      final String CRNL = "\r\n";
      String content = getContent(new FileInputStream(getResourceFile(requestFile)));
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(getServerHost(), 8080));
      OutputStream out = socket.getOutputStream();

      // send an HTTP request to the endpoint
      out.write(("POST /jaxws-jbws1582/TestService HTTP/1.0" + CRNL).getBytes());
      out.write(("Host: " + getServerHost() + ":8080" + CRNL).getBytes());
      out.write(("Content-Type: text/xml" + CRNL).getBytes());
      out.write(("Content-Length: " + content.length() + CRNL).getBytes());
      out.write((CRNL).getBytes());
      out.write((content).getBytes());

      // read the response
      String response = getContent(socket.getInputStream());
      socket.close();
      System.out.println("---");
      System.out.println(response);
      System.out.println("---");
      return response;
   }

   public void testAttackedArchiveDeployment() throws Exception
   {
      try
      {
         deploy("jaxws-jbws1582-attacked.war");
         if (isIntegrationCXF())
         {
            // Apache CXF ignores DOCTYPE section in WSDLs
            // so this attack is not doable on it.
         }
         else
         {
            // JBossWS Native stack throws exception for attacking WSDLs
            fail("deployment failure expected");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         log.warn(e.getMessage(), e);
      }
      finally
      {
         undeploy("jaxws-jbws1582-attacked.war");
      }
   }

   private static String getContent(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, is);
      return new String(baos.toByteArray());
   }

}
