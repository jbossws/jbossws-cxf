/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.Configuration;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

//TODO: reuse jaas test
public final class JaspiAuthenticationTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-jaspi";

   public static Test suite()
   {
      TestSetup testSetup = new JBossWSCXFTestSetup(JaspiAuthenticationTestCase.class, "jaxws-samples-wsse-policy-username-jaspi.war, jaxws-samples-wsse-policy-username-jaspi-client.war") {

         public void setUp() throws Exception
         {
            Map<String, String> loginModuleOptions = new HashMap<String, String>();
            String usersPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.users.propfile");
            String rolesPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.roles.propfile");
            if (usersPropFile != null)
            {
               loginModuleOptions.put("usersProperties", usersPropFile);
            }
            if (rolesPropFile != null)
            {
               loginModuleOptions.put("rolesProperties", rolesPropFile);
            }

            Map<String, String> authModuleOptions = new HashMap<String, String>();
            JBossWSTestHelper.addJaspiSecurityDomain("jaspi", "jaas-lm-stack", loginModuleOptions, "org.jboss.wsf.stack.cxf.jaspi.module.UsernameTokenServerAuthModule",
                  authModuleOptions);
            JBossWSTestHelper.addJaspiSecurityDomain("clientJaspi", "jaas-lm-stack", loginModuleOptions, "org.jboss.wsf.stack.cxf.client.jaspi.module.SOAPClientAuthModule",
                  authModuleOptions);
            super.setUp();
         }

         public void tearDown() throws Exception
         {
            JBossWSTestHelper.removeSecurityDomain("jaspi");
            JBossWSTestHelper.removeSecurityDomain("clientJaspi");
            super.tearDown();

         }
      };
      return testSetup;
   }
   
   public void testAuthenticated() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   public void testUnauthenticated() throws Exception
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
   public void testClientAuthModule() throws Exception
   {   
      //load client side jaspi config
      XMLLoginConfigImpl xli = XMLLoginConfigImpl.getInstance();
      Configuration.setConfiguration(xli);   
      URL configURL = Thread.currentThread().getContextClassLoader()
            .getResource("org/jboss/test/ws/jaxws/samples/wsse/policy/jaspi/config/jaspi-config-client.xml");
      xli.setConfigURL(configURL);
      xli.loadConfig();
      
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");     
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   
   
   public void testInContainerClientAuthModule() throws Exception
   {
      Helper helper = new Helper();
      helper.setTargetEndpoint("http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-jaspi");
      assertEquals("1", runTestInContainer("testJaspiClient"));
   }
   
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-wsse-policy-username-jaspi-client?path=/jaxws-samples-wsse-policy-username-jaspi&method=" + test
            + "&helper=" + Helper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
   

   private void setupWsse(ServiceIface proxy, String username)
   {   
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext()
            .put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.UsernamePasswordCallback");
   }
}
