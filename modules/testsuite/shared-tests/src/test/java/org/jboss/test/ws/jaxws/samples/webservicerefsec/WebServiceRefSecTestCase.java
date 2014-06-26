/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test multiple webserviceref fro the same endpoint with different security credentials
 *
 * @author alessio.soldano@jboss.com
 * @since 12-May-2010
 */
public class WebServiceRefSecTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-webservicerefsec";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-samples-webservicerefsec.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointImpl.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-webservicerefsec-servlet-client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Client.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(WebServiceRefSecTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()), true);
   }

   public void testServletClient() throws Exception
   {
      URL url = new URL(TARGET_ENDPOINT_ADDRESS + "-servlet-client?echo=HelloWorld");
      assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
   }
}
