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
package org.jboss.test.ws.jaxws.samples.session;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

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
            .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf\n"))
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
