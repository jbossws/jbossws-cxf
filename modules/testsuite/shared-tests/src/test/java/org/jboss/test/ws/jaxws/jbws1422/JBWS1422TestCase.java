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
package org.jboss.test.ws.jaxws.jbws1422;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1422] NPE if @WebParam.name like "mX.."
 * 
 * @author Thomas.Diesler@jboss.com 
 */
@RunWith(Arquillian.class)
public class JBWS1422TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1422.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvc.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvcImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testDeployment() throws Exception
   {
      QName serviceName = new QName("http://jbws1422.jaxws.ws.test.jboss.org/", "JBWS1422Service");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1422/JBWS1422Service/IWebsvcImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IWebsvc port = service.getPort(IWebsvc.class);
      String result = port.cancel("myFooBar");
      assertEquals("Cancelled-myFooBar", result);
   }
}
