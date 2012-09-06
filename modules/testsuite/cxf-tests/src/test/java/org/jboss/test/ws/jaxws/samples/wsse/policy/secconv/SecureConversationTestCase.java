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
package org.jboss.test.ws.jaxws.samples.wsse.policy.secconv;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Secure Conversation testcase
 *
 * From OASIS WS-SecurityPolicy Examples Version 1.0:
 * 
 * 2.4.1 (WSS 1.0) Secure Conversation bootstrapped by Mutual
 * Authentication with X.509 Certificates
 *
 * @author alessio.soldano@jboss.com
 * @since 06-Sep-2012
 */
public final class SecureConversationTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-secconv/SecureConversationService";

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(SecureConversationTestCase.class, "jaxws-samples-wsse-policy-secconv.war,jaxws-samples-wsse-policy-secconv-client.jar");
   }
   
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      assertTrue(proxy.sayHello().startsWith("Secure Conversation Hello World!"));
      assertTrue(proxy.sayHello().startsWith("Secure Conversation Hello World!"));
   }

   private void setupWsse(ServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put("ws-security.callback-handler.sct", new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put("ws-security.signature.properties.sct", Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put("ws-security.encryption.properties.sct", Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put("ws-security.signature.username.sct", "alice");
      ((BindingProvider)proxy).getRequestContext().put("ws-security.encryption.username.sct", "bob");
   }
}
