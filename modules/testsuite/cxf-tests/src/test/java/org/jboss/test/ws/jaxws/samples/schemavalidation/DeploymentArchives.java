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
package org.jboss.test.ws.jaxws.samples.schemavalidation;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-schemavalidation.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.HelloImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.ValidatingHelloImpl.class)
               .addPackage("org.jboss.test.ws.jaxws.samples.schemavalidation.types")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/WEB-INF/web.xml"));
         }
   });
   
   public static final String CLIENT_WAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-schemavalidation-client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.schemavalidation.Helper.class)
               .addPackage("org.jboss.test.ws.jaxws.samples.schemavalidation.types")
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/client.wsdl"), "classes/client.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/validatingClient.wsdl"), "classes/validatingClient.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/META-INF/permissions.xml"), "permissions.xml");
         }
   });
   
   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-schemavalidation-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/schemavalidation/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
   });

   private DeploymentArchives() {
      //NO OP
   }
}
