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
package org.jboss.test.ws.jaxws.jbws3282;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the handlers (pre/post) declaration in jaxws endpoint configuration
 * 
 * https://issues.jboss.org/browse/JBWS-3282
 * https://issues.jboss.org/browse/JBWS-3836
 *
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2014
 */
public class HandlerChainTestCaseForked extends JBossWSTest
{
   private final String targetNS = "http://jbws3282.jaxws.ws.test.jboss.org/";
   
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3832-f-inContainer-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.TestUtils.class)
               .addClass(org.jboss.test.helper.ClientHelper.class)
               .addClass(org.jboss.test.helper.TestServlet.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/META-INF/permissions.xml"), "permissions.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   private static String DEP = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws3282-f.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint2Impl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint3Impl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3282.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3282/jaxws-handlers-server.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/WEB-INF/web-f.xml"));
         }
      });

   public static Test suite()
   {
      return new JBossWSTestSetup(HandlerChainTestCaseForked.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testHandlerChainVanillaServer() throws Exception
   {
      try {
         JBossWSTestHelper.deploy(DEP);
         
         QName serviceName = new QName(targetNS, "Endpoint2ImplService");
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3282-f/ep2?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         String resStr = port.echo("Kermit");
         assertEquals("Kermit|EpIn|endpoint2|EpOut", resStr);
         
         serviceName = new QName(targetNS, "Endpoint3ImplService");
         wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3282-f/ep3?wsdl");
         service = Service.create(wsdlURL, serviceName);
         port = (Endpoint)service.getPort(Endpoint.class);
         resStr = port.echo("Kermit");
         assertEquals("Kermit|EpIn|endpoint3|EpOut", resStr);
      } finally {
         JBossWSTestHelper.undeploy(DEP);
      }
   }

   public void testHandlerChainModifiedServer() throws Exception
   {
      try {
         assertEquals("1", runTestInContainer("setupConfigurations"));
         try {
            JBossWSTestHelper.deploy(DEP);
            
            QName serviceName = new QName(targetNS, "Endpoint2ImplService");
            URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3282-f/ep2?wsdl");
            Service service = Service.create(wsdlURL, serviceName);
            Endpoint port = (Endpoint)service.getPort(Endpoint.class);
            String resStr = port.echo("Kermit");
            assertEquals("Kermit|EpIn|RoutIn|endpoint2|RoutOut|EpOut", resStr);
            
            serviceName = new QName(targetNS, "Endpoint3ImplService");
            wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3282-f/ep3?wsdl");
            service = Service.create(wsdlURL, serviceName);
            port = (Endpoint)service.getPort(Endpoint.class);
            resStr = port.echo("Kermit");
            assertEquals("Kermit|LogIn|EpIn|endpoint3|EpOut|LogOut", resStr);
         } finally {
            JBossWSTestHelper.undeploy(DEP);
         }
      } finally {
         assertEquals("1", runTestInContainer("restoreConfigurations"));
      }
   }

   // -------------------------
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-jbws3832-f-inContainer-client?path=/jaxws-jbws3282-f&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
