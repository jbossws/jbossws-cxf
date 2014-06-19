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
package org.jboss.test.ws.jaxws.jbws3287;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Verifies deployment descriptor support for jbossws-config-file / jbossws-config-name
 * 
 * https://issues.jboss.org/browse/JBWS-3287
 *
 * @author alessio.soldano@jboss.com
 * @since 25-May-2012
 */
public class JBWS3287TestCase extends JBossWSTest
{
   private static final String targetNS = "http://jbws3287.jaxws.ws.test.jboss.org/";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws3287-C.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EJB3EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/META-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3287-B.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/web-B.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3287-A.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/web-A.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3287TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testJBossWebservicesXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-A/TestService?wsdl");
   }
   
   public void testWebXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-B/TestService?wsdl");
   }
   
   public void testEJB3JBossWebservicesXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-C/EndpointImplService/Endpoint?wsdl");
   }
   
   private void runTestInternal(String path) throws Exception {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/" + path);

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|RoutIn|AuthIn|EpIn|LogIn|endpoint|LogOut|EpOut|AuthOut|RoutOut", resStr);
   }
}
