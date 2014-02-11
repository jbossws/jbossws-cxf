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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.wsf.test.CryptoHelper;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface;

/**
 * WS-Trust test case
 * This is basically the Apache CXF STS demo (from distribution samples)
 * ported to jbossws-cxf for running over JBoss Application Server.
 *
 * @author alessio.soldano@jboss.com
 * @since 08-Feb-2012
 */
public class WSTrustTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust/SecurityService";
   private final String stsURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-policy-trust-sts/SecurityTokenService";

   public static Test suite()
   {
      //deploy client, STS and service; start a security domain to be used by the STS for authenticating client
      return WSTrustTestUtils.getTestSetup(WSTrustTestCase.class,
            "jaxws-samples-wsse-policy-trust-client.jar jaxws-samples-wsse-policy-trust-sts.war jaxws-samples-wsse-policy-trust.war");
   }
   
   /**
    * WS-Trust test with the STS information programmatically provided
    * 
    * @throws Exception
    */
   public void test() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         WSTrustTestUtils.setupWsseAndSTSClient(proxy, bus, stsURL + "?wsdl", stsServiceName, stsPortName);

         try {
            assertEquals("WS-Trust Hello World!", proxy.sayHello());
         } catch (Exception e) {
            throw CryptoHelper.checkAndWrapException(e);
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   /**
    * WS-Trust test with the STS information coming from EPR specified in service endpoint contract policy
    * 
    * @throws Exception
    */
   public void testUsingEPR() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         WSTrustTestUtils.setupWsse(proxy, bus);
         
         try {
            assertEquals("WS-Trust Hello World!", proxy.sayHello());
         } catch (Exception e) {
            throw CryptoHelper.checkAndWrapException(e);
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   /**
    * No CallbackHandler is provided in STSCLient.  Username and password provided instead.
    *
    * @throws Exception
    */
   public void testNoClientCallback() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         WSTrustTestUtils.setupWsseAndSTSClientNoCallbackHandler(proxy, bus, stsURL + "?wsdl", stsServiceName, stsPortName);

         assertEquals("WS-Trust Hello World!", proxy.sayHello());
      } finally {
         bus.shutdown(true);
      }
   }

   /**
    * No SIGNATURE_USERNAME is provided to the service.  Service will use the
    * client's keystore alias in its place.
    *
    * @throws Exception
    */
   public void testNoSignatureUsername() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         WSTrustTestUtils.setupWsseAndSTSClientNoSignatureUsername(proxy, bus, stsURL + "?wsdl", stsServiceName, stsPortName);

         assertEquals("WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }

}
