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
package org.jboss.test.ws.jaxws.cxf.endpoint;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
@RunWith(Arquillian.class)
public class EndpointTestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-endpoint";
   //TODO! figure out proper way for getting the address
   private static String publishURL = "http://" + getServerHost() + ":48084/HelloWorldService";

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = DEP, testable = false, managed = false)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.common\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.endpoint.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.endpoint.HelloWorldImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.endpoint.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endpoint/WEB-INF/permissions.xml"), "permissions.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endpoint/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testClassLoader() throws Exception
   {
      deployer.deploy(DEP);
      HelloWorld port = this.getProxy(publishURL);
      String classLoader1 = port.getClassLoader();
      String deploymentClassLoader1 = port.getDeploymentClassLoader();
      deployer.undeploy(DEP);
      assertEquals(classLoader1, deploymentClassLoader1);
      deployer.deploy(DEP);
      port = this.getProxy(publishURL);
      String classLoader2 = port.getClassLoader();
      String deploymentClassLoader2 = port.getDeploymentClassLoader();
      deployer.undeploy(DEP);
      assertEquals(classLoader2, deploymentClassLoader2);
      assertFalse(classLoader1.equals(classLoader2));
   }

   private HelloWorld getProxy(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/cxf/endpoint", "HelloWorldService");
      Service service = Service.create(wsdlURL, qname);
      return (HelloWorld) service.getPort(HelloWorld.class);
   }
}
