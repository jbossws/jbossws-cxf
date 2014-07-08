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
package org.jboss.test.ws.jaxws.cxf.wsrm;

import java.io.File;

import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String RPC_SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-wsrm-basic-rpc.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicRPCEndpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicRPCEndpointImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-rpc/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-rpc/WEB-INF/web.xml"));
      }
   });
   
   public static final String DOC_SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-cxf-wsrm-basic-doc.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicDocEndpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.wsrm.BasicDocEndpointImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-doc/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/basic-doc/WEB-INF/web.xml"));
      }
   });
   
   public static final String CLIENT = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-wsrm-basic-client.jar") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/wsrm/cxf.xml"), "cxf.xml");
      }
   });

   public DeploymentArchives() {
      //NOOP
   }
}
