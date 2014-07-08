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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class UsernameDeploymentArchives
{
   public static String SERVER_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-username.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServerUsernamePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/web.xml"));
         }
      });
   public static String CLIENT_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-username-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security,org.jboss.ws.cxf.jbossws-cxf-client services,org.apache.cxf.impl\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.UsernameHelper.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.UsernamePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class);
         }
      });
   
   private UsernameDeploymentArchives() {
      //NOOP
   }
}
