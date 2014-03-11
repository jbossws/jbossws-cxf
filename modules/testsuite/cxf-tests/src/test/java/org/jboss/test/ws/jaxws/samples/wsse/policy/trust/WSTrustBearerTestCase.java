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
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.ClientCallbackHandler;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;


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

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      JBossWSCXFTestSetup testSetup = WSTrustTestUtils.getTestSetup(WSTrustBearerTestCase.class,
            "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts-bearer.war jaxws-samples-wsse-policy-trust-bearer.war");

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

         STSClient stsClient = new STSClient(bus);

         ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
         ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
         ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
         ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");
         
         ctx.put(SecurityConstants.STS_CLIENT, stsClient);


         proxy.sayHello();

      } catch(Exception e){
         e.printStackTrace();
         assertTrue(false);
      }
   }
   
   private static String appendIssuedTokenSuffix(String prop)
   {
      return prop + ".it";
   }

}
