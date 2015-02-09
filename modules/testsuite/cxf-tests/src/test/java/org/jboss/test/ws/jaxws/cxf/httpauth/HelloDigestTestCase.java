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
package org.jboss.test.ws.jaxws.cxf.httpauth;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.auth.DigestAuthSupplier;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author ema@redhat.com
 * @author alessio.soldano@jboss.com
 */
@Ignore(value="[JBWS-3620] Authentication failures w/ Undertow")
@RunWith(Arquillian.class)
public class HelloDigestTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-digest-sec.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.Hello.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.ObjectFactory.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/digest/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/digest/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testDigest() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         Hello proxy = (Hello)service.getPort(Hello.class);
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "jbossws");
         HTTPConduit cond = (HTTPConduit)ClientProxy.getClient(proxy).getConduit();
         cond.setAuthSupplier(new DigestAuthSupplier());
         int result = proxy.helloRequest("number");
         assertEquals(100, result);
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testDigestAuthFail() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
         URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         Hello proxy = (Hello)service.getPort(Hello.class);
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "wrongPwd");
         HTTPConduit cond = (HTTPConduit)ClientProxy.getClient(proxy).getConduit();
         cond.setAuthSupplier(new DigestAuthSupplier());
         try {
            proxy.helloRequest("number");
            fail("Authorization exception expected!");
         } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("Authorization"));
         }
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testDigestNoAuth() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
      try {
         proxy.helloRequest("number");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }
}