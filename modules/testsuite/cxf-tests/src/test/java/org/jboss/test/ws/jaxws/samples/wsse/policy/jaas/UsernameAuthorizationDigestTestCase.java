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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Security Policy username test case leveraging JAAS container integration and using digest passwords.
 * WS-SecurityPolicy 1.2 used for policies in the included wsdl contract.
 *
 * @author alessio.soldano@jboss.com
 * @since 26-May-2011
 */
public final class UsernameAuthorizationDigestTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-username-jaas-digest";

   public static Test suite()
   {
      JBossWSCXFTestSetup testSetup;
      if (!isTargetJBoss6())
      {
         testSetup = new JBossWSCXFTestSetup(UsernameAuthorizationDigestTestCase.class, "jaxws-samples-wsse-policy-username-jaas-digest.war");
         Map<String, String> authenticationOptions = new HashMap<String, String>();
         authenticationOptions.put("usersProperties",
               getResourceFile("jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties").getAbsolutePath());
         authenticationOptions.put("rolesProperties",
               getResourceFile("jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties").getAbsolutePath());
         authenticationOptions.put("hashAlgorithm", "SHA");
         authenticationOptions.put("hashEncoding", "BASE64");
         authenticationOptions.put("hashCharset", "UTF-8");
         authenticationOptions.put("hashUserPassword", "false");
         authenticationOptions.put("hashStorePassword", "true");
         authenticationOptions.put("storeDigestCallback", UsernameTokenCallback.class.getName());
         authenticationOptions.put("unauthenticatedIdentity", "anonymous");
         testSetup.addSecurityDomainRequirement("JBossWSDigest", authenticationOptions);
      }
      else
      {
         testSetup = new JBossWSCXFTestSetup(UsernameAuthorizationDigestTestCase.class,
            "jaxws-samples-wsse-policy-username-jaas-digest-service.sar jaxws-samples-wsse-policy-username-jaas-digest.war");
      }
      return testSetup;
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
   
   public void testUnauthorized() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "kermit");
      try
      {
         proxy.greetMe();
         fail("User kermit shouldn't be authorized to call greetMe().");
      }
      catch (Exception e)
      {
         assertEquals("Unauthorized", e.getMessage());
      }
   }

   private void setupWsse(ServiceIface proxy, String username)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, username);
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.UsernameDigestPasswordCallback");
   }
}
