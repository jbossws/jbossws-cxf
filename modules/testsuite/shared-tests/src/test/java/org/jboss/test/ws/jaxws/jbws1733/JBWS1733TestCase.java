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
package org.jboss.test.ws.jaxws.jbws1733;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1733] JBoss WebService client not sending JSESSIONID cookie after 2 calls
 *
 * @author ropalka@redhat.com
 * @since 09-Aug-2007
 */
@RunWith(Arquillian.class)
public class JBWS1733TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1733.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1733.JBWS1733.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1733.JBWS1733Impl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1733/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://jbws1733.jaxws.ws.test.jboss.org/", "JBWS1733Service");
      URL wsdlURL = new URL(baseURL + "/JBWS1733Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JBWS1733 proxy = (JBWS1733)service.getPort(JBWS1733.class);
      
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      for (int i = 1; i <= 10; i++)
      {
         assertEquals(i, proxy.getCounter());
      }
   }

}
