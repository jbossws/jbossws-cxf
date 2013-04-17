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
package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Security Policy examples
 *
 * From OASIS WS-SecurityPolicy Examples Version 1.0
 * http://docs.oasis-open.org/ws-sx/security-policy/examples/ws-sp-usecases-examples.html
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Sep-2012
 */
public final class WSSecurityPolicyExamples21xTestCase extends JBossWSTest
{
   private final String NS = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples";
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-oasis-21x/";
   private final String serviceURLHttps = "https://" + getServerHost() + ":8443/jaxws-samples-wsse-policy-oasis-21x/";
   private final QName serviceName = new QName(NS, "SecurityService");

   public static Test suite()
   {
      /** System properties - currently set at testsuite start time 
      System.setProperty("javax.net.ssl.trustStore", "my.truststore");
      System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
      System.setProperty("javax.net.ssl.trustStoreType", "jks");
      System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
      */
      JBossWSCXFTestSetup setup = new JBossWSCXFTestSetup(WSSecurityPolicyExamples21xTestCase.class,
         "jaxws-samples-wsse-policy-oasis-21x.war,jaxws-samples-wsse-policy-oasis-client.jar");
      Map<String, String> sslOptions = new HashMap<String, String>();
      if (isTargetJBoss7())
      {
         sslOptions.put("certificate-key-file", System.getProperty("org.jboss.ws.testsuite.server.keystore"));
         sslOptions.put("password", "changeit");
         sslOptions.put("verify-client", "false");
         sslOptions.put("key-alias", "tomcat");
      }
      else
      {
         sslOptions.put("server-identity.ssl.keystore-path", System.getProperty("org.jboss.ws.testsuite.server.keystore"));
         sslOptions.put("server-identity.ssl.keystore-password", "changeit");
         sslOptions.put("server-identity.ssl.alias", "tomcat");
      }
      setup.setHttpsConnectorRequirement(sslOptions);
      return setup;
   }
   
   /**
    * 2.1.1.1 UsernameToken with plain text password
    * 
    * @throws Exception
    */
   public void test2111() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2111?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2111Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken with plain text password"));
   }

   /**
    * 2.1.1.2 UsernameToken without password
    * 
    * @throws Exception
    */
   public void test2112() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2112?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2112Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken without password"));
   }

   /**
    * 2.1.1.3  UsernameToken with timestamp, nonce and password hash
    * 
    * @throws Exception
    */
   public void test2113() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService2113?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2113Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken with timestamp, nonce and password hash"));
   }

   /**
    * 2.1.2.1  UsernameToken as supporting token
    * 
    * @throws Exception
    */
   public void test2121() throws Exception
   {
      Service service = Service.create(new URL(serviceURLHttps + "SecurityService2121?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService2121Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - UsernameToken as supporting token"));
   }

   /**
    * 2.1.3  (WSS 1.0) UsernameToken with Mutual X.509v3 Authentication, Sign, Encrypt
    * 
    * @throws Exception
    */
   public void test213() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService213?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService213Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - (WSS 1.0) UsernameToken with Mutual X.509v3 Authentication, Sign, Encrypt"));
   }

   /**
    * 2.1.4  (WSS 1.1) User Name with Certificates, Sign, Encrypt
    * 
    * @throws Exception
    */
   public void test214() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService214?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService214Port"), ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().equals("Hello - (WSS 1.1) User Name with Certificates, Sign, Encrypt"));
   }

   private void setupWsse(ServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, "kermit");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new UsernamePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
   }
}
