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

import java.io.InputStream;

import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.ClientCallbackHandler;
import org.jboss.wsf.stack.cxf.client.configuration.BeanCustomizer;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSConfigurer;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Element;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;


/**
 * A demo of using SAML Bearer key type
 *
 * User: rsearls@redhat.com
 * Date: 2/24/14
 */
public class WSTrustBearerTestCase extends JBossWSTest
{

   private final String serviceURL = "http://" + getServerHost()
      + ":8080/jaxws-samples-wsse-policy-trust-bearer/BearerService";
   private final String stsURL = "http://" + getServerHost()
      + ":8080/jaxws-samples-wsse-policy-trust-sts-bearer/SecurityTokenService";

   public static Test suite()
   {
      /**  ***/
      // this must be set before testSetup is returned
      System.setProperty("javax.net.ssl.trustStore", "/home/rsearls/j1/jbossws/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks");
      System.setProperty("javax.net.ssl.trustStorePassword", "stsspass");
      System.setProperty("javax.net.ssl.trustStoreType", "jks");
      System.setProperty("org.jboss.security.ignoreHttpsHost", "true");


      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      JBossWSCXFTestSetup testSetup = WSTrustTestUtils.getTestSetup(WSTrustBearerTestCase.class,
            "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts-bearer.war jaxws-samples-wsse-policy-trust-bearer.war");


      /**
      // the server looks for the file /home/rsearls/.keystore no matter what
      // path given for keystore-path.  Fails to deploy war; test never runs
      Map<String, String> sslOptions = new HashMap<String, String>();
      sslOptions.put("server-identity.ssl.keystore-path", "servicestore.jks");
      sslOptions.put("server-identity.ssl.keystore-password", "sspass");
      sslOptions.put("authentication.truststore.keystore-path", "stsstore.jks");
      sslOptions.put("authentication.truststore.keystore-password", "stsspass");
      testSetup.setHttpsConnectorRequirement(sslOptions);
      **/
      return testSetup;
   }

   public void testAllInOneBearer() throws Exception
   {

      Bus bus = BusFactory.newInstance().createBus();
      try
      {

         BusFactory.setThreadDefaultBus(bus);

         //------------------------------
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         BearerIface proxy = (BearerIface) service.getPort(BearerIface.class);

         Map<String, Object> ctx = ((BindingProvider)proxy).getRequestContext();

         //jaxws-samples-wsse-policy-trust-sts-bearer
         // 8443
         STSClient stsClient = new STSClient(bus);
         /**/
            //stsClient.setWsdlLocation("https://localhost:8443/jaxws-samples-wsse-policy-trust-sts-bearer/SecurityTokenService/UT?wsdl");
                     //-stsClient.setWsdlLocation("http://localhost:8080/jaxws-samples-wsse-policy-trust-sts-bearer/SecurityTokenService/UT?wsdl");

         //WEB-INF/wsdl/bearer-ws-trust-1.4-service.wsdl
         URL stsWsdlURL =
         Thread.currentThread().getContextClassLoader().getResource("META-INF/bearer-ws-trust-1.4-service.wsdl");
         System.out.println("## stsWsdlURL: " + stsWsdlURL.toString());
            //.getResourceAsStream("WEB-INF/wsdl/bearer-ws-trust-1.4-service.wsdl");
         stsClient.setWsdlLocation(stsWsdlURL.toString());

         //stsClient.setWsdlLocation("http://localhost:8080/jaxws-samples-wsse-policy-trust-sts-bearer/SecurityTokenService?wsdl");

            stsClient.setServiceName("{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}SecurityTokenService");
            stsClient.setEndpointName("{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}UT_Port");

         //stsClient.setServiceName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/SecurityTokenService");
         //   stsClient.setEndpointName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/UT_Port");
         /**/
         Map<String, Object> props = stsClient.getProperties();
         props.put(SecurityConstants.USERNAME, "alice");
         props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
         props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
         props.put(SecurityConstants.STS_TOKEN_PROPERTIES,
            Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

         ctx.put(SecurityConstants.STS_CLIENT, stsClient);


         proxy.sayHello();

      } catch(Exception e){
         e.printStackTrace();
         assertTrue(false);
      }
   }

   public void XX_testBearer() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
  /**  **/
         // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         //-ServiceInfo servInfo = new ServiceInfo();
         //-servInfo.setTargetNamespace("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy");
         //-servInfo.setName(new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService"));

         ConduitInitiatorManager mgr = bus.getExtension(ConduitInitiatorManager.class);
         ConduitInitiator ci = null;
         ci = mgr.getConduitInitiator("http://cxf.apache.org/transports/http");

         URL myWsdlURL = new URL(serviceURL + "?wsdl");
         EndpointInfo endpointInfo = new EndpointInfo();
         endpointInfo.setName(new QName("http://cxf.apache.org", "TransportURIResolver"));
         endpointInfo.setAddress(myWsdlURL.toURI().toString());
         HTTPConduit httpConduit = new URLConnectionHTTPConduit(bus, endpointInfo,
            endpointInfo.getTarget());

         TLSClientParameters tlsParams = new TLSClientParameters();
         tlsParams.setSecureSocketProtocol("TLSv1");  // SSL .. try this
         setKeyManagers(tlsParams, "ckpass", "META-INF/clientstore.jks");
         tlsParams.setDisableCNCheck(true);

         /**  fix this
          * FiltersType filter = new FiltersType();
          filter.getInclude().add(".*_EXPORT_.*");
          filter.getInclude().add(".*_EXPORT1024_.*");
          filter.getInclude().add(".*_WITH_DES_.*");
          filter.getInclude().add(".*_WITH_NULL_.*");
          filter.getExclude().add(".*_DH_anon_.*");
          tlsParams.setCipherSuitesFilter(filter);

          */
         httpConduit.setTlsClientParameters(tlsParams);

         JBossWSConfigurer configurer = (JBossWSConfigurer)bus.getExtension(Configurer.class);
         BeanCustomizer customizer = configurer.getCustomizer();
         customizer.customize(httpConduit);

         // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


         BusFactory.setThreadDefaultBus(bus);

         //------------------------------
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         BearerIface proxy = (BearerIface) service.getPort(BearerIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientBearer((BindingProvider) proxy, bus);

          assertEquals("Bearer WS-Trust Hello World!", proxy.sayHello());

           //- proxy.sayHello();


      } catch (Exception e) {    // rls added
         e.printStackTrace(); // rls added
         assertTrue(false); // rls added
      }  // rls added

      finally
      {
         bus.shutdown(true);
      }
   }


   //
   private TLSClientParameters setKeyManagers(TLSClientParameters tlsParams,
                                              String keyPassword, String keyStoreLoc)
      throws KeyStoreException, Exception {
      keyStoreLoc =  "META-INF/clientstore.jks";
      InputStream inStream = Thread.currentThread().getContextClassLoader()
         .getResourceAsStream(keyStoreLoc);
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inStream, "cspass".toCharArray());
      inStream.close();


      String alg = KeyManagerFactory.getDefaultAlgorithm();
      char[] keyPass = keyPassword != null
         ? keyPassword.toCharArray()
         : null;
      KeyManagerFactory keyMF = KeyManagerFactory.getInstance(alg);
      keyMF.init(keyStore, keyPass);
      KeyManager[] myKeyManagers =  keyMF.getKeyManagers();
      tlsParams.setKeyManagers(myKeyManagers);


      inStream = Thread.currentThread().getContextClassLoader()
         .getResourceAsStream(keyStoreLoc);
      KeyStore trustStore = KeyStore.getInstance("JKS");
      trustStore.load(inStream, "cspass".toCharArray());
      inStream.close();
      TrustManagerFactory trustMF = TrustManagerFactory.getInstance(alg);
      trustMF.init(trustStore);
      TrustManager[] myTrustStoreKeyManagers = trustMF.getTrustManagers();
      tlsParams.setTrustManagers(myTrustStoreKeyManagers);
      return tlsParams;
   }

}
