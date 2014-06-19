/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2634;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.test.ws.jaxws.jbws2634.webservice.EndpointIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2634] Implement support for @EJB annotations in WS components
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class JBWS2634TestCase extends JBossWSTest
{
   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2634-pojo.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.logging\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.POJOBean.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-handler.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/WEB-INF/web.xml"));
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2634.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.BeanIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.BeanImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.handlers.TestHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.AbstractEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EndpointIface.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2634-ejb3.jar") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.logging\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EJB3Bean.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-handler.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/META-INF/ejb-jar.xml"), "ejb-jar.xml");
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2634.ear") { {
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/META-INF/application.xml"), "application.xml")
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634-pojo.war"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634-ejb3.jar"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634.jar"));
         }
      });
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2634TestCase.class, "jaxws-jbws2634.ear");
   }

   public void testPojoEndpointInjection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "POJOService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2634-pojo/POJOService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:POJOBean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   public void testEjb3EndpointInjection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "EJB3Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2634-ejb3/EJB3Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:EJB3Bean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }
}
