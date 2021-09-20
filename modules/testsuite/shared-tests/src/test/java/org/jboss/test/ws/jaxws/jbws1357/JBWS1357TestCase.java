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
