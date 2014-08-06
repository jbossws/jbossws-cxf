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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test to demonstrate enable session with cxf <code>@FactoryType</code>
 * annotation
 * 
 * @author ema@redhat.com
 */
public class SessionEndpointTestCase extends JBossWSTest
{
   private String targetNS = "http://jboss.org/jaxws-samples-session";

   private SessionEndpoint proxy;

   public static BaseDeployment<?>[] createDeployments()
   {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-session.war")
      {
         {
            archive
                  .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf.impl\n"))
                  .addClass(org.jboss.test.ws.jaxws.samples.session.SessionEndpoint.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.session.SessionEndpointImpl.class)
                  .setWebXML(
                        new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/session/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(SessionEndpointTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

   }

   public void testSession() throws Exception
   {
      SessionEndpoint proxy = this.createPort();
      SessionEndpoint proxy2 = this.createPort();
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://localhost:8080/jaxws-samples-session/session");
      ((BindingProvider) proxy2).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      ((BindingProvider) proxy2).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://localhost:8080/jaxws-samples-session/session");
      proxy.setNumber(10);
      assertEquals("Number is 10", proxy.getNumber());
      proxy2.setNumber(20);
      assertEquals("Number is 20", proxy2.getNumber());
      assertEquals("Number is 10", proxy.getNumber());
   }

   public SessionEndpoint createPort() throws Exception
   {
      QName serviceName = new QName(targetNS, "SessionService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-session/session?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (SessionEndpoint) service.getPort(SessionEndpoint.class);
      return proxy;
   }

}
