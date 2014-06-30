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
package org.jboss.test.ws.jaxws.cxf.spring;

import java.io.File;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   private static final File SPRING_DIR = new File(new File(JBossWSTestHelper.getTestResourcesDir()).getParentFile(), "spring");
   
   public static final String SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-spring.war") { {
            archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.spring.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.spring.EndpointImpl.class);
         }
   });
   
   public static final String CLIENT = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-spring-client.war") { {
            archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.spring.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.spring.Foo.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.spring.Helper.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/my-cxf.xml"), "my-cxf.xml")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/spring-dd.xml"), "spring-dd.xml")
               .addAsLibrary(new File(SPRING_DIR, "spring-aop-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-asm-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-beans-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-context-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-core-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-expression-3.0.3.RELEASE.jar"))
               .addAsLibrary(new File(SPRING_DIR, "spring-web-3.0.3.RELEASE.jar"));
         }
   });
   
   private DeploymentArchives() {
      //NOOP
   }
}
