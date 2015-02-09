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
package org.jboss.test.ws.jaxws.jbws2074.usecase2.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.ws.jaxws.jbws2074.usecase2.service.EJB3Iface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2074] Resource injection in jaxws endpoints and handlers
 *
 * @author ropalka@redhat.com
 */
@RunWith(Arquillian.class)
public final class JBWS2074TestCase extends JBossWSTest
{
   private static final String JAR_DEPLOYMENT = "jaxws-jbws2074-usecase2";
   private static final String EAR_DEPLOYMENT = "jaxws-jbws2074-ear-usecase2";

   @ArquillianResource
   Deployer deployer;
   
   private static JavaArchive getJarArchive() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JAR_DEPLOYMENT + ".jar");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase2.service.EJB3Iface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase2.service.EJB3Impl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase2/META-INF/ejb-jar.xml"), "ejb-jar.xml");
      return archive;
   }

   @Deployment(name = JAR_DEPLOYMENT, testable = false, managed = false, order = 1)
   public static JavaArchive createClientDeployment1() {
      return getJarArchive();
   }

   @Deployment(name = EAR_DEPLOYMENT, testable = false, managed = false, order = 2)
   public static EnterpriseArchive createClientDeployment() {
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws2074-usecase2.ear");
      archive
         .addManifest()
         .addAsModule(getJarArchive())
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase2-ear/META-INF/application.xml"), "application.xml");
      return archive;
   }

   public void executeTest() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2074-usecase2/Service";
      QName serviceName = new QName("http://ws.jboss.org/jbws2074", "EJB3Service");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      EJB3Iface port = (EJB3Iface)service.getPort(EJB3Iface.class);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":EJB3Impl");
      assertEquals(expStr.toString(), retStr);
   }

   @Test
   @RunAsClient
   public void testUsecase2WithoutEar() throws Exception
   {
      try
      {
         deployer.deploy(JAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(JAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testUsecase2WithEar() throws Exception
   {
      try
      {
         deployer.deploy(EAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(EAR_DEPLOYMENT);
      }
   }

}
