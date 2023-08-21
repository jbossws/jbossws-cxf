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
package org.jboss.test.ws.jaxws.jbws1357;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * [JBWS-1357] JAXWSDeployerJSE is not handling jsp servlet defs correctly
 * 
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class JBWS1357TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1357.war");
         archive
               .addManifest()
               .addAsWebResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1357/hello.jsp"))
               .addClass(org.jboss.test.ws.jaxws.jbws1357.JBWS1357.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1357.JBWS1357Impl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1357/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEcho() throws Exception
   {
      QName serviceName = new QName("http://jbws1357.jaxws.ws.test.jboss.org/", "JBWS1357Service");
      URL wsdlURL = new URL(baseURL + "/JBWS1357Service?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      JBWS1357 proxy = (JBWS1357)service.getPort(JBWS1357.class);
      
      assertEquals("hi there", proxy.echo("hi there"));
   }

   @Test
   @RunAsClient
   public void testJSP() throws Exception
   {
      URL jsp = new URL(baseURL + "/hello.jsp");
      HttpURLConnection conn = (HttpURLConnection) jsp.openConnection();
      assertEquals(conn.getResponseCode(), 200);
      IOUtils.readAndCloseStream(conn.getInputStream());
   }
}
