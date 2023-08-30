/*
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

@RunWith(Arquillian.class)
public class JBWS3679TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3679.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.CDIBeanClient.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOne.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.EndpointOneService.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3679.ServletClient.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3679/WEB-INF/beans.xml"), "beans.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3679/WEB-INF/web.xml"))
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3679/WEB-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testServletClient() throws Exception
   {
      URL url = new URL(baseURL + "/ServletClient");
      assertEquals("Echoded with:input", IOUtils.readAndCloseStream(url.openStream()));
   }

   @Test
   @RunAsClient
   public void testCDIClient() throws Exception
   {
      URL url = new URL(baseURL + "/ServletClient?client=CDI");
      assertEquals("Echoded with:cdiInput", IOUtils.readAndCloseStream(url.openStream()));
   }
}
