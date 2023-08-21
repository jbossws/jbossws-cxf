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
package org.jboss.test.ws.jaxws.smoke.tools;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class WSProviderTestCaseForked extends PluginBase
{
   @Deployment(name="jaxws-classloading-types", order=1, testable = false)
   public static JavaArchive createDeployment1() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-classloading-types.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.Echo.class)
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.EchoResponse.class)
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.Message.class);
      return archive;
   }

   @Deployment(name="jaxws-classloading-service", order=2, testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-classloading-service.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.HelloWorld.class);
      return archive;
   }

   /**
    * Recreates a tools delegate for every test
    * @throws Exception
    */
   @Before
   public void setup() throws Exception
   {
      setupClasspath();

      Class<?> wspClass = Thread.currentThread().getContextClassLoader().loadClass(WSProviderPlugin.class.getName());
      setDelegate(wspClass);
    }


   @After
   public void teardown() throws Exception
   {
      restoreClasspath();
   }

   @Test
   @RunAsClient
   public void testGenerateWsdl() throws Exception
   {
      dispatch("testGenerateWsdl");
   }

   @Test
   @RunAsClient
   public void testGenerateWsdlWithExtension() throws Exception
   {
      dispatch("testGenerateWsdlWithExtension");
   }

   @Test
   @RunAsClient
   public void testGenerateSource() throws Exception
   {
      dispatch("testGenerateSource");
   }

   @Test
   @RunAsClient
   public void testOutputDirectory() throws Exception
   {
      dispatch("testOutputDirectory");
   }

   @Test
   @RunAsClient
   public void testResourceDirectory() throws Exception
   {
      dispatch("testResourceDirectory");
   }

   @Test
   @RunAsClient
   public void testSourceDirectory() throws Exception
   {
      dispatch("testSourceDirectory");
   }

   @Test
   @RunAsClient
   public void testClassLoader() throws Exception
   {
      dispatch("testClassLoader");
   }

   @Test
   @RunAsClient
   public void testMessageStream() throws Exception
   {
      dispatch("testMessageStream");
   }
}
