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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
/**
 * @author ema@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class HelloBasicSecTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-cxf-basic-sec";
   
   public static Test suite()
   {
      JBossWSCXFTestSetup testSetup;
      testSetup = new JBossWSCXFTestSetup(HelloBasicSecTestCase.class, "jaxws-cxf-basic-sec.war");
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/cxf/httpauth/WEB-INF/ws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/cxf/httpauth/WEB-INF/ws-roles.properties").getAbsolutePath());
      authenticationOptions.put("password-stacking", "useFirstPass");
      testSetup.addSecurityDomainRequirement("ws-basic-domain", authenticationOptions);
      return testSetup;
   }

   public void testBasicAuth() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "jbossws");
      int result = proxy.helloRequest("number");
      assertEquals(100, result);
      
   }
   
   public void testBasicAuthFail() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jbossws");
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "wrongPwd");
      try {
         proxy.helloRequest("number");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }
   
   public void testBasicNoAuth() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      try {
         proxy.helloRequest("number");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }
}