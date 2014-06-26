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
package org.jboss.test.ws.jaxws.smoke.tools;

import java.util.LinkedList;
import java.util.List;

import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * @author Heiko.Braun@jboss.com
 */
public class WSProviderTestCaseForked extends PluginBase
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-classloading-types.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.Echo.class)
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.EchoResponse.class)
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.Message.class);
         }
      });
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-classloading-service.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.smoke.tools.service.HelloWorld.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   /**
    * Recreates a tools delegate for every test
    * @throws Exception
    */
   protected void setUp() throws Exception
   {

      setupClasspath();

      Class<?> wspClass = Thread.currentThread().getContextClassLoader()
        .loadClass("org.jboss.test.ws.jaxws.smoke.tools.WSProviderPlugin");
      setDelegate(wspClass);
      
      JBossWSTestHelper.writeToFile(createDeployments());
    }


   protected void tearDown() throws Exception
   {
      restoreClasspath();
   }

   public void testGenerateWsdl() throws Exception
   {
      dispatch("testGenerateWsdl");
   }
   
   public void testGenerateWsdlWithExtension() throws Exception
   {
      dispatch("testGenerateWsdlWithExtension");
   }

   public void testGenerateSource() throws Exception
   {
      dispatch("testGenerateSource");
   }

   public void testOutputDirectory() throws Exception
   {
      dispatch("testOutputDirectory");
   }

   public void testResourceDirectory() throws Exception
   {
      dispatch("testResourceDirectory");
   }

   public void testSourceDirectory() throws Exception
   {
      dispatch("testSourceDirectory");
   }

   public void testClassLoader() throws Exception
   {
      dispatch("testClassLoader");
   }

   public void testMessageStream() throws Exception
   {
      dispatch("testMessageStream");
   }

   /**
    * Filter sun jaxws implementation because it clashes
    * with the native one (ServiceLoader...)
    * @param jarName
    * @return
    */
   protected boolean filtered(String jarName)
   {
      return false;
   }
}
