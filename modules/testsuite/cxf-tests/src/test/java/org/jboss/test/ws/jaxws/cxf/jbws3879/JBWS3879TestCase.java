/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3879;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 12-Mar-2015
 *
 */
@RunWith(Arquillian.class)
public class JBWS3879TestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-jbws3879";
   private static final String CLIENT_DEP = "jaxws-cxf-jbws3879-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3879.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3879.HelloWorldImpl.class)
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3879/WEB-INF/jaxws-endpoint-config.xml")), "jaxws-endpoint-config.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3879/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services,org.apache.cxf.impl\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3879.GZIPEnforcingInInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3879.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3879.Helper.class)
            .addAsResource(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3879/WEB-INF/jaxws-client-config.xml")), "jaxws-client-config.xml")
            .addClass(org.jboss.wsf.test.ClientHelper.class)
            .addClass(org.jboss.wsf.test.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3879/WEB-INF/permissions.xml"), "permissions.xml");
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPUsingFeatureOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPUsingFeatureOnClient"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPServerSideOnlyInterceptorOnClient"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testFailureGZIPServerSideOnlyInterceptorOnClient"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-jbws3879/HelloWorldService/HelloWorldImpl&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
