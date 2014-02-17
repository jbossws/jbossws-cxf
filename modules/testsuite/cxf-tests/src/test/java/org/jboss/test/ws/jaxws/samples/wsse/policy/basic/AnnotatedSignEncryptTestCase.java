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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.wsf.test.CryptoHelper;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-SecurityPolicy code first dev test
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Jun-2013
 */
public final class AnnotatedSignEncryptTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-sign-encrypt-gcm-code-first/AnnotatedSecurityService";

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(AnnotatedSignEncryptTestCase.class, "jaxws-samples-wsse-policy-sign-encrypt-gcm-code-first.war jaxws-samples-wsse-policy-sign-encrypt-client.jar");
   }

   public void testWsdl() throws Exception
   {
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      BufferedReader br = new BufferedReader(new InputStreamReader(wsdlURL.openStream(), "UTF-8"));
      StringBuilder sb = new StringBuilder();
      try {
         String s;
         while ((s = br.readLine()) != null) {
            sb.append(s);
         }
      } finally {
         br.close();
      }
      String wsdl = sb.toString();
      assertTrue(wsdl.contains("AsymmetricBinding_X509v1_GCM256OAEP_ProtectTokens_binding_policy"));
   }
   
   public void test() throws Exception
   {
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "AnnotatedSecurityService");
         URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         AnnotatedServiceIface proxy = (AnnotatedServiceIface)service.getPort(AnnotatedServiceIface.class);
         setupWsse(proxy);
         assertEquals("Secure Hello World!", proxy.sayHello());
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   private void setupWsse(AnnotatedServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
   }
}
