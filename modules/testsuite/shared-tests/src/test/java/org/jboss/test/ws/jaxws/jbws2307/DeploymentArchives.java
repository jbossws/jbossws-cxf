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
package org.jboss.test.ws.jaxws.jbws2307;

import java.io.File;

import org.jboss.wsf.test.JBossWSTestHelper;

public final class DeploymentArchives
{
   public static final String SERVER = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-service.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"), "web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"));
      }
   });
   
   public static final String CLIENT_2 = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-client-2.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet2.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloService.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web2.xml"), "web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web2.xml"));
      }
   });
   
   public static final String CLIENT_3 = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-client-3.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet3.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloServiceJAXWS22.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web3.xml"), "web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web3.xml"));
      }
   });
   
   public static final String CLIENT = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-client.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloServiceJAXWS22.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"), "web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"));
      }
   });
    
   private DeploymentArchives() {
      //NOOP
   }
}
