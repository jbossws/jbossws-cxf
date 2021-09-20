/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3556;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS3556TestCase extends JBossWSTest {

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3556.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3556.EndpointIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3556.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3556.MyException.class);
      return archive;
   }

    private EndpointIface getProxy() throws Exception {
        final URL wsdlURL = new URL(baseURL + "/EndpointImpl?wsdl");
        final QName serviceName = new QName("http://jbws3556.jaxws.ws.test.jboss.org/", "EndpointImplService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }

   @Test
   @RunAsClient
    public void testException() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwException();
            fail("Expected exception not thrown");
        } catch (MyException e) {
            assertEquals("from 1,1,message 1,summary 1", e.toString());
        }
    }
}
