/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3628;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testcase for system property expansion support in WSDL documents.
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jul-2014
 */
@RunWith(Arquillian.class)
public class JBWS3628TestCase extends JBossWSTest
{
   private static final String POLICY_NAME = "WS-Addressing_policy";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3628.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3628.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3628.CheckInterceptor.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3628/WEB-INF/wsdl/service.wsdl"), "wsdl/service.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDL() throws Exception {
      URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
      checkPolicyReference(wsdlURL, POLICY_NAME);
   }
   
   @Test
   @RunAsClient
   public void testInvocation() throws Exception {
      URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/jbws3628", "ServiceOne"));
      EndpointOne port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/jbws3628", "EndpointOnePort"), EndpointOne.class);
      assertEquals("Foo", port.echo("Foo"));
   }
   
   private void checkPolicyReference(URL wsdlURL, String refId) throws Exception {
      final String wsdl = IOUtils.readAndCloseStream(wsdlURL.openStream());
      assertTrue("WSDL does not contain policy reference to '" + refId + "'", wsdl.contains("<wsp:PolicyReference URI=\"#" + refId + "\">"));
   }
   
}
