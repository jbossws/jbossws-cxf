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
package org.jboss.test.ws.jaxws.samples.session;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to demonstrate enable session with cxf <code>@FactoryType</code>
 * annotation
 * 
 * @author ema@redhat.com
 */
@RunWith(Arquillian.class)
public class SessionEndpointTestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-session.war");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf.impl\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.session.SessionEndpoint.class)
            .addClass(org.jboss.test.ws.jaxws.samples.session.SessionEndpointImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/session/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSession() throws Exception
   {
      SessionEndpoint proxy = this.createPort();
      SessionEndpoint proxy2 = this.createPort();
      String addr = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-session/session";
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);
      ((BindingProvider) proxy2).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      ((BindingProvider) proxy2).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);
      proxy.setNumber(10);
      assertEquals("Number is 10", proxy.getNumber());
      proxy2.setNumber(20);
      assertEquals("Number is 20", proxy2.getNumber());
      assertEquals("Number is 10", proxy.getNumber());
   }

   public SessionEndpoint createPort() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/jaxws-samples-session", "SessionService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-session/session?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      SessionEndpoint proxy = (SessionEndpoint) service.getPort(SessionEndpoint.class);
      return proxy;
   }

}
