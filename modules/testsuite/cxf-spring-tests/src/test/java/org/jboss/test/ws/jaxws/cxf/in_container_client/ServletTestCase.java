/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.in_container_client;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * A testcase for verifying a cxf.xml Spring descriptor based Bus can
 * successfully be created and used in a in-container client.
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-May-2013
 *
 */
public class ServletTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-in_container_client-client.war") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/cxf.xml"), "cxf.xml")
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.Helper.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/WEB-INF/jboss-deployment-structure.xml"), "jboss-deployment-structure.xml");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-in_container_client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorldImpl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ServletTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   public void test() throws Exception
   {
      assertEquals("1", runTestInContainer("test"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-cxf-in_container_client-client?path=/jaxws-cxf-in_container_client/HelloWorldService&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
