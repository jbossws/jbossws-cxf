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
package org.jboss.test.ws.jaxws.date;

import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A simple test with endpoint getting/sending Date objects marshalled
 * to siple yyyy-MM-dd HH:mm:ss format.
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Apr-2015
 */
@RunWith(Arquillian.class)
public class DateTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-date.war");
         archive
            .addManifest()
            .addPackages(false, new Filter<ArchivePath>() {
               @Override
               public boolean include(ArchivePath path)
               {
                  return !path.get().contains("TestCase");
               }
            }, "org.jboss.test.ws.jaxws.date");
      return archive;
   }

   @Test
   @RunAsClient
   public void testDate() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/MyService?wsdl");
      QName qname = new QName("http://date.jaxws.ws.test.jboss.org/", "MyService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = service.getPort(Endpoint.class);

      Date date = new Date();
      
      Date response = port.echoDate(date);
      assertEquals(date.toString(), response.toString());
      
   }
}
