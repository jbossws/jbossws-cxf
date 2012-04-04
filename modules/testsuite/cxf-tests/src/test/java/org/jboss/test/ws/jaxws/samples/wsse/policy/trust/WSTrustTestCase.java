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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * WS-Trust test case
 * This is basically the Apache CXF STS demo (from distribution samples)
 * ported to jbossws-cxf for running over JBoss Application Server.
 *
 * @author alessio.soldano@jboss.com
 * @since 08-Feb-2012
 */
public final class WSTrustTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust/SecurityService";
   private final String stsURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust-sts/SecurityTokenService";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      JBossWSCXFTestSetup testSetup = new JBossWSCXFTestSetup(
            WSTrustTestCase.class, "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts.war jaxws-samples-wsse-policy-trust.war");
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties").getAbsolutePath());
      authenticationOptions.put("unauthenticatedIdentity", "anonymous");
      testSetup.addSecurityDomainRequirement("JBossWS-trust-sts", authenticationOptions);
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

   private void setupWsse(ServiceIface proxy, Bus bus)
   {
      Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
      ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
      ctx.put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
      ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
      ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
      STSClient stsClient = new STSClient(bus);
      stsClient.setWsdlLocation(stsURL + "?wsdl");
      stsClient.setServiceQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService"));
      stsClient.setEndpointQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port"));
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
}
