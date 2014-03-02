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

import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.ClientCallbackHandler;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;

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
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      return WSTrustTestUtils.getTestSetup(WSTrustBearerTestCase.class,
            "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts-bearer.war jaxws-samples-wsse-policy-trust-bearer.war");
   }

   /**
    * @throws Exception
    */
   public void TMP_DISABLE_testBearer() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         BearerIface proxy = (BearerIface) service.getPort(BearerIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientBearer((BindingProvider) proxy, bus);

         assertEquals("Bearer WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   /**
    * TMEP: Just for isolating testing of STS' generation of Bearer security token
    * @throws Exception
    */
   public void testSTSBearer() throws Exception
   {
      final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
      final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");


      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         STSClient stsClient = new STSClient(bus);
         stsClient.setWsdlLocation(stsURL + "?wsdl");
         stsClient.setServiceQName(stsServiceName);
         stsClient.setEndpointQName(stsPortName);

         stsClient.setTokenType("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
         stsClient.setKeyType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");

         stsClient.setAddressingNamespace("http://www.w3.org/2005/08/addressing");

         Map<String, Object> props = stsClient.getProperties();
         props.put(SecurityConstants.USERNAME, "alice");
         props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
         props.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
         props.put(SecurityConstants.SIGNATURE_PROPERTIES,
            Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         //props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");


          try {
            //- rls test  -- NOTE: using https and NOT http here.
            SecurityToken st = stsClient.requestSecurityToken(
               "https://localhost:8080/jaxws-samples-wsse-policy-trust-bearer/myBearerService");
          } catch (Exception e){
            System.out.println("##Error: " + e);
            e.printStackTrace();
          }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
}
