/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3827;

import junit.framework.Test;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.apache.cxf.ws.security.SecurityConstants;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test imported wsdl identified by URL of deployed app.
 */
public class JWBS3827TestCase extends JBossWSTest
{
   private final String serviceURL = "https://" + getServerHost()
      + ":8443/jaxws-jbws3827-wsse-policy-username";

   public static JBossWSTestHelper.BaseDeployment<?>[] createDeployments() {
      List<JBossWSTestHelper.BaseDeployment<?>> list = new LinkedList<JBossWSTestHelper.BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3827-wsse-policy-username.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.ServerUsernamePasswordCallback.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.SayHelloResponse.class)

            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3827/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3827/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3827/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3827/WEB-INF/web.xml"));
      }
      });

      /**/
      list.add(
         new JBossWSTestHelper.WarDeployment("jbws3827-wsdlImport.war") {
            {
               archive
                  .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
                  .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.GreetingsWsImpl.class)
                  .addClass(org.jboss.test.ws.jaxws.cxf.jbws3827.GreetingsWs.class)

                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                     + "/jaxws/cxf/jbws3827/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                     + "/jaxws/cxf/jbws3827/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties")

                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                     + "/jaxws/cxf/jbws3827/WEB-INF/wsdl/Greeting_Simplest.wsdl"),
                     "wsdl/Greeting_Simplest.wsdl")
               ;
            }
         });

      /**/

      return list.toArray(new JBossWSTestHelper.BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      /**
      //System properties - currently set at testsuite start time
       System.setProperty("javax.net.ssl.trustStore", JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3827/WEB-INF/servicestore.jks");
       System.setProperty("javax.net.ssl.trustStorePassword", "sspass");
       System.setProperty("javax.net.ssl.trustStoreType", "jks");
       System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
       **/
      JBossWSTestSetup setup = new JBossWSCXFTestSetup(JWBS3827TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
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

   /**
   public void testPre() throws Exception {

      URL wsdlURL = new URL("http://" + getServerHost()
         + ":8443/jaxws-jbws3827-wsse-policy-username/SecurityService?wsdl");
      QName qname = new QName(
         "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy",
         "SecurityService");
      Service service = Service.create(wsdlURL, qname);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   public void testAfter() throws Exception {

      URL wsdlURL = new URL("http://" + getServerHost()
         + ":8080/jbws3827-wsdlImport/GreetingsService?wsdl");
      QName qname = new QName(
         "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy",
         "SecurityService");
      Service service = Service.create(wsdlURL, qname);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   **/

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put(
         SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext().put(
         SecurityConstants.CALLBACK_HANDLER,
         "org.jboss.test.ws.jaxws.cxf.jbws3827.UsernamePasswordCallback");
   }
}
