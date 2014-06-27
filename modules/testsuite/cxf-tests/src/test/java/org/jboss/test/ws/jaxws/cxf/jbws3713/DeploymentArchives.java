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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3713.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWSImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema2.xsd"), "wsdl/Hello_schema2.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema3.xsd"), "wsdl/Hello_schema3.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema4.xsd"), "wsdl/Hello_schema4.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema5.xsd"), "wsdl/Hello_schema5.xsd");
         }
      });

   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3713-client.jar") { {
            archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Main-Class: org.jboss.test.ws.jaxws.cxf.jbws3713.TestClient\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl,org.jboss.ws.jaxws-client\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.BusCounter.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelperUsignThreadLocal.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.TestClient.class);
         }
      });

   public static final String CLIENT_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3713-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.BusCounter.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.ClientServlet.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.ClientServletUsignThreadLocal.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelperUsignThreadLocal.class);
         }
      });

}
