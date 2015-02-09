/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2527;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JBWS-2527 testcase: BeanFactory not initialized or already closed
 * 
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS2527TestCase extends JBossWSTest
{
   @ArquillianResource
   Deployer deployer;

   @Deployment(name="jaxws-jbws2527-service", managed=false, testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2527-service.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2527.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloImpl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloService.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/web.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-jbws2527-client", managed=false, testable = false)
   public static WebArchive createClientDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2527-client.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2527.ClientServlet.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2527.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/web.xml"), "web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/web.xml"));
      return archive;
   }

  @Test
  @RunAsClient
   public void test() throws Exception
   {
      for (int i = 0; i < 2; i++)
      {
         executeTest();
         executeTest();
      }
   }

   public void executeTest() throws Exception
   {
      try
      {
         deployer.deploy("jaxws-jbws2527-service");
         deployer.deploy("jaxws-jbws2527-client");
         assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2527-client/jbws2527").openStream()));
      }
      finally
      {
         deployer.undeploy("jaxws-jbws2527-client");
         deployer.undeploy("jaxws-jbws2527-service");
      }
   }
}
