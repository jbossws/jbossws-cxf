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
package org.jboss.test.ws.jaxws.cxf.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

/**
 * Test case for publishing a JMS (SOAP-over-JMS 1.0) endpoint through API 
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class JMSEndpointAPITestCase extends JBossWSTest
{
   private static final String JMS_SERVER = "jms";
   
   private static boolean useHornetQ() {
      return JBossWSTestHelper.isTargetWildFly9();
   }

   @Deployment(testable = false)
   @TargetsContainer(JMS_SERVER)
   public static WebArchive createWarDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-cxf-jms-api.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services," + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis")))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl"), "classes/META-INF/wsdl/HelloWorldService.wsdl")
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorldImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.TestServlet.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER) + "/jaxws-cxf-jms-api");
      assertEquals("true", IOUtils.readAndCloseStream(url.openStream()));
   }
   
}
