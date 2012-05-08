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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trustPicketLink;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Trust test case using PicketLink implementation of STS
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Apr-2012
 */
public final class WSTrustPicketLinkTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust/SecurityService";
   private final String stsURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trustPicketLink-sts/PicketLinkSTS";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      JBossWSCXFTestSetup testSetup = new JBossWSCXFTestSetup(
            WSTrustPicketLinkTestCase.class, "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trustPicketLink-sts.war jaxws-samples-wsse-policy-trust.war", true);
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties").getAbsolutePath());
      authenticationOptions.put("unauthenticatedIdentity", "anonymous");
      testSetup.addSecurityDomainRequirement("JBossWS-trustPicketLink-sts", authenticationOptions);
      return testSetup;
   }

   public void test() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         setupWsse(proxy, bus);
         assertEquals("WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   private void setupWsse(ServiceIface proxy, Bus bus) throws Exception
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
      STSClient stsClient = new STSClient(bus);
      stsClient.setWsdlLocation(stsURL + "?wsdl");
      stsClient.setServiceQName(new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTS"));
      stsClient.setEndpointQName(new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTSPort"));
      Map<String, Object> props = stsClient.getProperties();
//      props.put(SecurityConstants.USERNAME, "alice");
//      props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
//      props.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
//      props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
      props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
      props.put(SecurityConstants.STS_TOKEN_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");

      //set http basic auth to workaround PicketLink STS requiring username to be set in context; this needs to be performed in a CXF specific way,
      //as the CXF STSClient does not support setting up basic auth by simple BindingProvider.USERNAME_PROPERTY/USERNAME_PASSWORD setup
      HTTPConduit conduit = (HTTPConduit)stsClient.getClient().getConduit();
      AuthorizationPolicy authPolicy = new AuthorizationPolicy();
      authPolicy.setAuthorizationType("BASIC");
      authPolicy.setUserName("alice");
      authPolicy.setPassword("clarinet");
      conduit.setAuthorization(authPolicy);
      
      ctx.put(SecurityConstants.STS_CLIENT, stsClient);
   }
}
