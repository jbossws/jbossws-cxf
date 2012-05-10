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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * Some client util methods for WS-Trust testcases 
 *
 * @author alessio.soldano@jboss.com
 * @since 08-May-2012
 */
public class WSTrustTestUtils
{
   public static JBossWSCXFTestSetup getTestSetup(Class<?> testClass, String archives) {
      JBossWSCXFTestSetup testSetup = new JBossWSCXFTestSetup(testClass, archives);
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            JBossWSTestHelper.getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            JBossWSTestHelper.getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties").getAbsolutePath());
      authenticationOptions.put("unauthenticatedIdentity", "anonymous");
      testSetup.addSecurityDomainRequirement("JBossWS-trust-sts", authenticationOptions);
      return testSetup;
   }
   
   public static void setupWsseAndSTSClient(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
      STSClient stsClient = new STSClient(bus);
      if (stsWsdlLocation != null) {
         stsClient.setWsdlLocation(stsWsdlLocation);
         stsClient.setServiceQName(stsService);
         stsClient.setEndpointQName(stsPort);
      }
      Map<String, Object> props = stsClient.getProperties();
      props.put(SecurityConstants.USERNAME, "alice");
      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }
   
   public static void setupWsse(ServiceIface proxy, Bus bus)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
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
   }
   
   private static String appendIssuedTokenSuffix(String prop)
   {
      return prop + ".it";
   }
}
