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

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback;
import org.jboss.wsf.test.CryptoHelper;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Security Policy examples
 *
 * From OASIS WS-SecurityPolicy Examples Version 1.0
 * http://docs.oasis-open.org/ws-sx/security-policy/examples/ws-sp-usecases-examples.html
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Sep-2012
 */
public final class WSSecurityPolicyExamples22xTestCase extends JBossWSTest
{
   private final String NS = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples";
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-oasis-22x/";
   private final QName serviceName = new QName(NS, "SecurityService");

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WSSecurityPolicyExamples22xTestCase.class,
            DeploymentArchives.SERVER_22X_WAR + " " + DeploymentArchives.CLIENT_JAR);
   }
   
   /**
    * 2.2.1 (WSS1.0) X.509 Certificates, Sign, Encrypt
    * 
    * This use-case corresponds to the situation where both parties have X.509v3 certificates (and public-private key pairs).
    * The requestor identifies itself to the service. The message exchange is integrity protected and encrypted.
    * 
    * @throws Exception
    */
   public void test221() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService221?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService221Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.2  (WSS1.0) Mutual Authentication with X.509 Certificates, Sign, Encrypt
    * 
    * This use case corresponds to the situation where both parties have X.509v3 certificates (and public-private key pairs).
    * The requestor wishes to identify itself to the service using its X.509 credential (strong authentication).
    * The message exchange needs to be integrity protected and encrypted as well. The difference from previous use case is
    * that the X509 token inserted by the client is included in the message signature (see <ProtectTokens />).
    * 
    * @throws Exception
    */
   public void test222() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService222?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService222Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.0) Mutual Authentication with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.3  (WSS1.1) Anonymous with X.509 Certificate, Sign, Encrypt
    * 
    * In this use case the Request is signed using DerivedKeyToken1(K), then encrypted using a DerivedKeyToken2(K) where K is ephemeral key
    * protected for the server's certificate. Response is signed using DKT3(K), (if needed) encrypted using DKT4(K). The requestor does no
    * wish to identify himself; the message exchange is protected using derived symmetric keys. As a simpler, but less secure, alternative,
    * ephemeral key K (instead of derived keys) could be used for message protection by simply omitting the sp:RequireDerivedKeys assertion.
    * 
    * @throws Exception
    */
   public void test223() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService223?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService223Port"), ServiceIface.class);
      setupWsse(proxy, true);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) Anonymous with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   /**
    * 2.2.4  (WSS1.1) Mutual Authentication with X.509 Certificates, Sign, Encrypt
    * 
    * Client and server X509 certificates are used for client and server authorization respectively. Request is signed using K, then
    * encrypted using K, K is ephemeral key protected for server's certificate. Signature corresponding to K is signed using client certificate.
    * Response is signed using K, encrypted using K, encrypted key K is not included in response. Alternatively, derived keys can be used for
    * each of the encryption and signature operations by simply adding an sp:RequireDerivedKeys assertion.
    * 
    * @throws Exception
    */
   public void test224() throws Exception
   {
      Service service = Service.create(new URL(serviceURL + "SecurityService224?wsdl"), serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(new QName(NS, "SecurityService224Port"), ServiceIface.class);
      setupWsse(proxy, false);
      try {
         assertTrue(proxy.sayHello().equals("Hello - (WSS1.1) Mutual Authentication with X.509 Certificates, Sign, Encrypt"));
      } catch (Exception e) {
         throw CryptoHelper.checkAndWrapException(e);
      }
   }

   private void setupWsse(ServiceIface proxy, boolean streaming)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
      if (streaming)
      {
         ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
         ((BindingProvider)proxy).getResponseContext().put(SecurityConstants.ENABLE_STREAMING_SECURITY, "true");
      }
   }
}
