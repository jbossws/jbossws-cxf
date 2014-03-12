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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.basic.UsernameOverTransportTestCase;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.ClientCallbackHandler;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSConfigurer;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.apache.cxf.service.model.EndpointInfo;
import java.io.InputStream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * A demo of using SAML Bearer key type
 *
 * User: rsearls@redhat.com
 * Date: 2/24/14
 */
public class WSTrustBearerTestCase extends JBossWSTest
{
   private final String serviceURL = "https://" + getServerHost()
      + ":8443/jaxws-samples-wsse-policy-trust-bearer/BearerService";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      JBossWSCXFTestSetup testSetup = WSTrustTestUtils.getTestSetup(WSTrustBearerTestCase.class,
         "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts-bearer.war jaxws-samples-wsse-policy-trust-bearer.war");

      // setup the https connector in the server config file.
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

      testSetup.setHttpsConnectorRequirement(sslOptions);
      return testSetup;

   }

   public void testBearer() throws Exception
   {

      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         // Must create and register conduit for https before creating service
         // and set in bus before setting default bus.
         setHTTPConduit(serviceURL, bus);
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         BearerIface proxy = (BearerIface) service.getPort(BearerIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientBearer((BindingProvider) proxy, bus);
         assertEquals("Bearer WS-Trust Hello World!", proxy.sayHello());

      } catch(Exception e){
         assertTrue("Failed test setup of conduit.", false);
      }
   }

   private void setHTTPConduit(String tmpServiceURL, Bus bus) throws Exception {

      URL myWsdlURL = new URL(tmpServiceURL + "?wsdl");
      EndpointInfo endpointInfo = new EndpointInfo();
      endpointInfo.setName(new QName("http://cxf.apache.org", "TransportURIResolver"));
      endpointInfo.setAddress(myWsdlURL.toURI().toString());
      HTTPConduit httpConduit = new URLConnectionHTTPConduit(bus, endpointInfo,
         endpointInfo.getTarget());

      TLSClientParameters tlsParams = new TLSClientParameters();
      tlsParams.setSecureSocketProtocol("SSL");
      setKeyManagers(tlsParams, "ckpass", "cspass", "META-INF/clientstore.jks");
      tlsParams.setDisableCNCheck(true);

      httpConduit.setTlsClientParameters(tlsParams);

      JBossWSConfigurer configurer = (JBossWSConfigurer)bus.getExtension(Configurer.class);
      BeanCustomizer customizer = configurer.getCustomizer();
      customizer.customize(httpConduit);
   }


   private TLSClientParameters setKeyManagers(TLSClientParameters tlsParams, String keyPassword,
                                              String keyStorePasswd, String keyStoreLoc) throws Exception {

      InputStream inStream = Thread.currentThread().getContextClassLoader()
         .getResourceAsStream(keyStoreLoc);
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inStream, keyStorePasswd.toCharArray());
      inStream.close();


      String alg = KeyManagerFactory.getDefaultAlgorithm();
      char[] keyPass = keyPassword != null
         ? keyPassword.toCharArray()
         : null;
      KeyManagerFactory keyMF = KeyManagerFactory.getInstance(alg);
      keyMF.init(keyStore, keyPass);
      KeyManager[] myKeyManagers = keyMF.getKeyManagers();
      tlsParams.setKeyManagers(myKeyManagers);

      inStream = Thread.currentThread().getContextClassLoader()
         .getResourceAsStream(keyStoreLoc);
      KeyStore trustStore = KeyStore.getInstance("JKS");
      trustStore.load(inStream, keyStorePasswd.toCharArray());
      inStream.close();
      TrustManagerFactory trustMF = TrustManagerFactory.getInstance(alg);
      trustMF.init(trustStore);
      TrustManager[] myTrustStoreKeyManagers = trustMF.getTrustManagers();
      tlsParams.setTrustManagers(myTrustStoreKeyManagers);
      return tlsParams;
   }

}
