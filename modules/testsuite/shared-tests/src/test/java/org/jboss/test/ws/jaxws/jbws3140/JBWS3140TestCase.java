/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3140;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

public class JBWS3140TestCase extends JBossWSTest
{
   public final String servletClientURL = "http://" + getServerHost() + ":8080/jbws3140-client/ServletTest";

   public void testWsaResponses() throws Exception
   {
      try {
         JBossWSTestHelper.deploy("jbws3140-responses-server.war");
         JBossWSTestHelper.deploy("jbws3140-client.war");
         HttpURLConnection connection = (HttpURLConnection) new URL(servletClientURL + "?mtom=small").openConnection();
         String result = readConnection(connection).toString();
         assertTrue("SOAPFaultException is expected but received: " + result, result.indexOf("SOAPFaultException") > -1);
         String expectedDetail = "A header representing a Message Addressing Property is not valid";
         assertTrue("Expected message wasn't found in response: " + result, result.indexOf(expectedDetail) > -1);
      } finally {
         JBossWSTestHelper.undeploy("jbws3140-responses-server.war");
         JBossWSTestHelper.undeploy("jbws3140-client.war");
      }
   }

   public void testMtomSmall() throws Exception
   {
      try {
         JBossWSTestHelper.deploy("jbws3140-server.war");
         JBossWSTestHelper.deploy("jbws3140-client.war");
         HttpURLConnection connection = (HttpURLConnection) new URL(servletClientURL + "?mtom=small").openConnection();
         String result = readConnection(connection).toString();
         String expected ="--ClientMTOMEnabled--ServerMTOMEnabled--ServerAddressingEnabled--ClientAddressingEnabled";
         assertTrue("Expected string wasn't found in response: " + result, result.indexOf(expected) > -1);
      } finally {
         JBossWSTestHelper.undeploy("jbws3140-server.war");
         JBossWSTestHelper.undeploy("jbws3140-client.war");
      }
   }

   private ByteArrayOutputStream readConnection(HttpURLConnection connection) 
   {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int count = 0;
      try
      {
         while( (count = connection.getInputStream().read(buffer, 0, 1024)) > -1) {
            bout.write(buffer, 0, count);
         }
      }
      catch (IOException e)
      {
      }
      return bout;
   }

}
