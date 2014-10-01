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
package org.jboss.test.ws.jaxws.cxf.in_container_client;
import static org.jboss.wsf.stack.cxf.client.Constants.JBWS_CXF_JAXWS_CLIENT_BUS_SELECTOR;

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
 * [JBWS-3832] Different default Spring descriptor name
 * for creating client and server Bus instances
 *
 */
public class CustomBusServletTestCaseForked extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
       
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-in_container_client-client_spring.war") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/cxf.xml"), "cxf.xml")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/cxf-client.xml"), "cxf-client.xml")
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.Helper.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/WEB-INF/jboss-deployment-structure.xml"), "jboss-deployment-structure.xml");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-in_container_client_spring.war") { {
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
      return new JBossWSCXFTestSetup(CustomBusServletTestCaseForked.class, JBossWSTestHelper.writeToFile(createDeployments())) {
          /// see https://issues.jboss.org/browse/JBWS-3832
          private static final String PROPERTY_NAME = JBWS_CXF_JAXWS_CLIENT_BUS_SELECTOR;
          private static final String SPRING_BUS_SELECTOR = "org.jboss.wsf.stack.cxf.client.SpringCustomClientBusSelector";

          private String formerValue;

          @Override
          public void setUp() throws Exception {
              formerValue = JBossWSTestHelper.setSystemProperty(PROPERTY_NAME, SPRING_BUS_SELECTOR);
              JBossWSTestHelper.restartServer();
              super.setUp();
          }

          @Override
          public void tearDown() throws Exception {
              super.tearDown();
              JBossWSTestHelper.setSystemProperty(PROPERTY_NAME, formerValue);
              JBossWSTestHelper.restartServer();
              formerValue = null;
          }
      };
   }
   
   public void test() throws Exception
   {
      assertEquals("1", runTestInContainer("testSpringBus"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-cxf-in_container_client-client_spring?path=/jaxws-cxf-in_container_client_spring/HelloWorldService&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
