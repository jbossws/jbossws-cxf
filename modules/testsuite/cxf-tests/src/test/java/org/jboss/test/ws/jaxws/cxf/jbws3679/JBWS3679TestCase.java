/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3679;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS3679TestCase extends JBossWSTest
{
   public final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3679/ServletClient";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3679.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.CDIBeanClient.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOne.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOneImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOneService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.ServletClient.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3679/WEB-INF/beans.xml"), "beans.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3679/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3679TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testServletClient() throws Exception
   {
      URL url = new URL(endpointAddress);
      assertEquals("Echoded with:input", IOUtils.readAndCloseStream(url.openStream()));
   }

   public void testCDIClient() throws Exception
   {
      URL url = new URL(endpointAddress + "?client=CDI");
      assertEquals("Echoded with:cdiInput", IOUtils.readAndCloseStream(url.openStream()));
   }
}
