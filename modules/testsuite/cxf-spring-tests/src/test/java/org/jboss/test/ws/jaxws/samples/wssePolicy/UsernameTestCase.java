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
package org.jboss.test.ws.jaxws.samples.wssePolicy;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * WS-Security Policy username test case
 *
 * @author alessio.soldano@jboss.com
 * @since 01-May-2009
 */
public final class UsernameTestCase extends JBossWSTest
{
   private final String serviceURL = "https://" + getServerHost() + ":8443/jaxws-samples-wssePolicy-username";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wssePolicy-username.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.ServerUsernamePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wssePolicy.jaxws.SayHelloResponse.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/username/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/username/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/username/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wssePolicy/username/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      /** System properties - currently set at testsuite start time 
      System.setProperty("javax.net.ssl.trustStore", "my.truststore");
      System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
      System.setProperty("javax.net.ssl.trustStoreType", "jks");
      System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
      */
      JBossWSTestSetup setup = new JBossWSCXFTestSetup(UsernameTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
      Map<String, String> sslOptions = new HashMap<String, String>();
      sslOptions.put("server-identity.ssl.keystore-path", System.getProperty("org.jboss.ws.testsuite.server.keystore"));
      sslOptions.put("server-identity.ssl.keystore-password", "changeit");
      sslOptions.put("server-identity.ssl.alias", "tomcat");
      setup.setHttpsConnectorRequirement(sslOptions);
      return setup;
   }

   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   public void testWrongPassword() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "snoopy");
      try
      {
         proxy.sayHello();
         fail("User snoopy shouldn't be authenticated.");
      }
      catch (Exception e)
      {
         //OK
      }
   }

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wssePolicy.UsernamePasswordCallback");
   }
}
