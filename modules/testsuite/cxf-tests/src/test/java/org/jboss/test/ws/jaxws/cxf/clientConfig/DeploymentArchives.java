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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

public class DeploymentArchives
{
   public static final String NAMES = JBossWSTestHelper.writeToFile(createDeployments());
   
   private static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-clientConfig.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.EndpointImpl.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-clientConfig-inContainer-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf.impl\n"))
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
               .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.TestUtils.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/permissions.xml"), "permissions.xml");
         }
      });
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-cxf-clientConfig-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

}
