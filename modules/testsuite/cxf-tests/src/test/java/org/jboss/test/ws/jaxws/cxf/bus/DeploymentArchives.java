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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-bus.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.ClientEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.ClientEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF/web.xml"));
         }
   });
   
   public static final String SERVER_2 = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-bus-wsdl.war") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/InvalidAddressEndpoint.wsdl"), "InvalidAddressEndpoint.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/ValidAddressEndpoint.wsdl"), "ValidAddressEndpoint.wsdl")
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.WSDLServlet.class);
         }
   });
   
   public static final String SERVLET_CLIENT = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-bus-servlet-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services export,com.sun.xml.messaging.saaj services\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.AbstractClient.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.BusTestException.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.ServletClient.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF-client/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/WEB-INF-client/web.xml"));
         }
   });

   public static final String EJB3_CLIENT = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-bus-ejb3-client.jar") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.AbstractClient.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.BusTestException.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.EJB3Client.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.EJB3ClientRemoteInterface.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.bus.EndpointService.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/bus/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
         }
   });
   
   private DeploymentArchives() {
      //NOOP
   }
}
