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
package org.jboss.test.ws.jaxws.cxf.jbws3805;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3805] Allow overriding soap:address rewrite options in jboss-webservices.xml
 *
 */
public class JBWS3805TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments()
   {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3805.war") {
         {
            archive.setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.common\n"))
                  .addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOne.class).addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOneImpl.class)
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
                  .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3805TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testWsdlSoapAddress() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3805/HelloService?wsdl");
      HttpURLConnection connection = (HttpURLConnection)wsdlURL.openConnection();
      try
      {
         connection.connect();
         assertEquals(200, connection.getResponseCode());
         connection.getInputStream();

         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String line;
         while ((line = in.readLine()) != null)
         {
            if (line.contains("address location"))
            {
               assertTrue("Unexpected uri scheme", line.contains("https://foo:8443/jaxws-cxf-JBWS3805/HelloService"));
               return;
            }
         }
         fail("Could not check soap:address!");
      }
      finally
      {
         connection.disconnect();
      }

   }

}
