/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import java.io.File;
import java.io.FilenameFilter;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [AS7-537] Filter Apache CXF and dependencies
 * 
 * Verifies deployment fails if the webservices subsystem is not disabled for the current deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2013
 */
@RunWith(Arquillian.class)
public class AS7537TestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-embedded-fail";
   @ArquillianResource
   Deployer deployer;
   
   @Deployment(name = DEP, testable = false, managed = false)
   public static WebArchive createDeployment() {
      final File springDir = new File(new File(JBossWSTestHelper.getTestResourcesDir()).getParentFile(), "spring");
      final File embeddedCXFDir = new File(new File(JBossWSTestHelper.getTestResourcesDir()).getParentFile(), "cxf-embedded");
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: javax.wsdl4j.api,org.apache.ws.xmlschema,org.apache.neethi,org.codehaus.woodstox\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.noIntegration.EchoImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/noIntegration/embedded/WEB-INF/beans.xml"), "beans.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/noIntegration/embedded/WEB-INF/web.xml"));
      JBossWSTestHelper.addLibrary(springDir, archive);
      JBossWSTestHelper.addLibrary(embeddedCXFDir, archive);
      return archive;
   }

   @Test
   @RunAsClient
   public void testFailureWithoutJBossDeploymentStructure() throws Exception {
      boolean undeploy = true;
      try {
         deployer.deploy(DEP);
         fail("Deployment failure expected");
      } catch (Exception e) {
         undeploy = false;
         assertTrue(e.getMessage().contains("JBAS015599") || e.getMessage().contains("WFLYWS0059"));
      } finally {
         if (undeploy) {
            try {
               deployer.undeploy(DEP);
            } catch (Exception e) {
               //ignore
            }
         }
      }
   }
}
