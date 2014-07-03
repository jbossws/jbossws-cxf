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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.Configuration;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
/**
 * TestCase to demonstrate jaspi authentication 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public final class JaspiAuthenticationTestCase extends JBossWSTest
{
   private final String serviceEndpointURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-endpoint-jaspi";
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-jbws-jaspi";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-username-jbws-jaspi.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF2/jboss-webservices.xml"), "jboss-webservices.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF2/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF2/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF2/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-username-jaspi-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl\n"))
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.UsernamePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
               .addClass(org.jboss.wsf.test.ClientHelper.class)
               .addClass(org.jboss.wsf.test.TestServlet.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-policy-username-endpoint-jaspi.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.ServiceEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/jaspi/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      TestSetup testSetup = new JBossWSCXFTestSetup(JaspiAuthenticationTestCase.class, JBossWSTestHelper.writeToFile(createDeployments())) {

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
            JBossWSTestHelper.addJaspiSecurityDomain("clientJaspi", "jaas-lm-stack", loginModuleOptions, "org.jboss.wsf.stack.cxf.jaspi.client.module.SOAPClientAuthModule",
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
   

   
   public void testWebserviceMDEnableAuthenticated() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   
 
   public void testEndpointEnableAuthenticated() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceEndpointURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   
   public void testUnauthenticated() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceEndpointURL + "?wsdl");
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
      URL wsdlURL = new URL(serviceEndpointURL + "?wsdl");     
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   
   
   public void testInContainerClientAuthModule() throws Exception
   {
      Helper helper = new Helper();
      helper.setTargetEndpoint("http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-endpoint-jaspi");
      assertEquals("1", runTestInContainer("testJaspiClient"));
   }
   
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-wsse-policy-username-jaspi-client?path=/jaxws-samples-wsse-policy-username-endpoint-jaspi&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
   

   private void setupWsse(ServiceIface proxy, String username)
   {   
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext()
            .put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.UsernamePasswordCallback");
   }
   
}
