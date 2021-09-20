/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsServiceIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceIface;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface;
import org.jboss.wsf.test.CryptoCheckHelper;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Trust test case
 * This is basically the Apache CXF STS demo (from distribution samples)
 * ported to jbossws-cxf for running over JBoss Application Server.
 *
 * @author alessio.soldano@jboss.com
 * @author rsearls@redhat.com
 * @since 08-Feb-2012
 */
@RunWith(Arquillian.class)
public class WSTrustTestCase extends JBossWSTest
{
   private static final String STS_DEP = "jaxws-samples-wsse-policy-trust-sts";
   private static final String SERVER_DEP = "jaxws-samples-wsse-policy-trust";
   private static final String ACT_AS_SERVER_DEP = "jaxws-samples-wsse-policy-trust-actas";
   private static final String ON_BEHALF_OF_SERVER_DEP = "jaxws-samples-wsse-policy-trust-onbehalfof";
   private static final String HOLDER_OF_KEY_STS_DEP = "jaxws-samples-wsse-policy-trust-sts-holderofkey";
   private static final String HOLDER_OF_KEY_SERVER_DEP = "jaxws-samples-wsse-policy-trust-holderofkey";
   private static final String PL_STS_DEP = "jaxws-samples-wsse-policy-trustPicketLink-sts";
   private static final String BEARER_STS_DEP = "jaxws-samples-wsse-policy-trust-sts-bearer";
   private static final String BEARER_SERVER_DEP = "jaxws-samples-wsse-policy-trust-bearer";

   @ArquillianResource
   private URL serviceURL;
   
   @Deployment(name = STS_DEP, testable = false)
   public static WebArchive createSTSDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, STS_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.jboss.ws.cxf.sts annotations\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.SampleSTS.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ws-trust-1.4-service.wsdl"), "wsdl/ws-trust-1.4-service.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = SERVER_DEP, testable = false)
   public static WebArchive createServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVER_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServerCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-policy-trust-client.jar") { {
            archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks");
         }
      });
   }

   @Deployment(name = ACT_AS_SERVER_DEP, testable = false)
   public static WebArchive createActAsServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, ACT_AS_SERVER_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.jboss.ws.cxf.sts\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.actas.ActAsServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ActAsService.wsdl"), "wsdl/ActAsService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/ActAsService_schema1.xsd"), "wsdl/ActAsService_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasstore.jks"), "classes/actasstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasKeystore.properties"), "classes/actasKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Deployment(name = ON_BEHALF_OF_SERVER_DEP, testable = false)
   public static WebArchive createOnBehalfOfServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, ON_BEHALF_OF_SERVER_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.jboss.ws.cxf.sts\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.onbehalfof.OnBehalfOfServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/OnBehalfOfService.wsdl"), "wsdl/OnBehalfOfService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/OnBehalfOfService_schema1.xsd"), "wsdl/OnBehalfOfService_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasstore.jks"), "classes/actasstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/actasKeystore.properties"), "classes/actasKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientstore.jks"), "clientstore.jks")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/clientKeystore.properties"), "clientKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Deployment(name = HOLDER_OF_KEY_STS_DEP, testable = false)
   public static WebArchive createHolderOfKeySTSDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, HOLDER_OF_KEY_STS_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.jboss.ws.cxf.sts annotations\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsholderofkey.STSHolderOfKeyCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsholderofkey.SampleSTSHolderOfKey.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/holderofkey-ws-trust-1.4-service.wsdl"), "wsdl/holderofkey-ws-trust-1.4-service.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/holderofkey/web.xml"));
      return archive;
   }

   @Deployment(name = HOLDER_OF_KEY_SERVER_DEP, testable = false)
   public static WebArchive createHolderOfKeyServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, HOLDER_OF_KEY_SERVER_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/HolderOfKeyService.wsdl"), "wsdl/HolderOfKeyService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/HolderOfKeyService_schema1.xsd"), "wsdl/HolderOfKeyService_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties");
      return archive;
   }

   @Deployment(name = PL_STS_DEP, testable = false)
   public static WebArchive createPicketLinkSTSDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, PL_STS_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.picketlink\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.picketlink.PicketLinkSTService.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/PicketLinkSTS.wsdl"), "wsdl/PicketLinkSTS.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/picketlink-sts.xml"), "classes/picketlink-sts.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties");
      return archive;
   }

   @Deployment(name = BEARER_STS_DEP, testable = false)
   public static WebArchive createBearerSTSDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, BEARER_STS_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.jboss.ws.cxf.sts annotations\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.STSBearerCallbackHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.SampleSTSBearer.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/bearer-ws-trust-1.4-service.wsdl"), "wsdl/bearer-ws-trust-1.4-service.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsstore.jks"), "classes/stsstore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/stsKeystore.properties"), "classes/stsKeystore.properties")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/META-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/bearer/web.xml"));
      return archive;
   }

   @Deployment(name = BEARER_SERVER_DEP, testable = false)
   public static WebArchive createBearerServerDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, BEARER_SERVER_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/BearerService.wsdl"), "wsdl/BearerService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/wsdl/BearerService_schema1.xsd"), "wsdl/BearerService_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/servicestore.jks"), "classes/servicestore.jks")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/policy/trust/WEB-INF/serviceKeystore.properties"), "classes/serviceKeystore.properties");
      return archive;
   }
   
   
   /**
    * WS-Trust test with the STS information programmatically provided
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEP)
   @WrapThreadContextClassLoader
   public void test() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "SecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         URL stsURL = new URL(serviceURL.getProtocol(), serviceURL.getHost(), serviceURL.getPort(), "/jaxws-samples-wsse-policy-trust-sts/SecurityTokenService?wsdl");
         WSTrustTestUtils.setupWsseAndSTSClient(proxy, bus, stsURL.toString(), stsServiceName, stsPortName);

         try {
            assertEquals("WS-Trust Hello World!", proxy.sayHello());
         } catch (Exception e) {
            throw CryptoCheckHelper.checkAndWrapException(e);
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
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testUsingEPR() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "SecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         WSTrustTestUtils.setupWsse(proxy, bus);
         
         try {
            assertEquals("WS-Trust Hello World!", proxy.sayHello());
         } catch (Exception e) {
            throw CryptoCheckHelper.checkAndWrapException(e);
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
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testNoClientCallback() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "SecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         URL stsURL = new URL(serviceURL.getProtocol(), serviceURL.getHost(), serviceURL.getPort(), "/jaxws-samples-wsse-policy-trust-sts/SecurityTokenService?wsdl");
         WSTrustTestUtils.setupWsseAndSTSClientNoCallbackHandler(proxy, bus, stsURL.toString(), stsServiceName, stsPortName);

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
   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testNoSignatureUsername() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "SecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         URL stsURL = new URL(serviceURL.getProtocol(), serviceURL.getHost(), serviceURL.getPort(), "/jaxws-samples-wsse-policy-trust-sts/SecurityTokenService?wsdl");
         WSTrustTestUtils.setupWsseAndSTSClientNoSignatureUsername(proxy, bus, stsURL.toString(), stsServiceName, stsPortName);

         assertEquals("WS-Trust Hello World!", proxy.sayHello());
      }
      finally
      {
         bus.shutdown(true);
      }
   }


   /**
    *  Request a security token that allows it to act as if it were somebody else.
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(ACT_AS_SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testActAs() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/actaswssecuritypolicy", "ActAsService");
         final URL wsdlURL = new URL(serviceURL + "ActAsService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ActAsServiceIface proxy = (ActAsServiceIface) service.getPort(ActAsServiceIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientActAs((BindingProvider) proxy, bus);

         assertEquals("ActAs WS-Trust Hello World!", proxy.sayHello(getServerHost(), String.valueOf(getServerPort())));
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   /**
    *  Request a security token that allows it to act on behalf of somebody else.
    *
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(ON_BEHALF_OF_SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testOnBehalfOf() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/onbehalfofwssecuritypolicy", "OnBehalfOfService");
         final URL wsdlURL = new URL(serviceURL + "OnBehalfOfService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         OnBehalfOfServiceIface proxy = (OnBehalfOfServiceIface) service.getPort(OnBehalfOfServiceIface.class);

         /* TODO explain why this is not needed for setup and then remove
         final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
         final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
         */
         WSTrustTestUtils.setupWsseAndSTSClientOnBehalfOf((BindingProvider) proxy, bus);

         assertEquals("OnBehalfOf WS-Trust Hello World!", proxy.sayHello(getServerHost(), String.valueOf(getServerPort())));
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(HOLDER_OF_KEY_SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testHolderOfKey() throws Exception
   {

      Bus bus = BusFactory.newInstance().createBus();
      try
      {

         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/holderofkeywssecuritypolicy", "HolderOfKeyService");
         final URL wsdlURL = new URL("https", serviceURL.getHost(), serviceURL.getPort() - 8080 + 8443, "/jaxws-samples-wsse-policy-trust-holderofkey/HolderOfKeyService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         HolderOfKeyIface proxy = (HolderOfKeyIface) service.getPort(HolderOfKeyIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientHolderOfKey((BindingProvider) proxy, bus);
         assertEquals("Holder-Of-Key WS-Trust Hello World!", proxy.sayHello());

      } finally
      {
         bus.shutdown(true);
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testPicketLink() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
         final URL wsdlURL = new URL(serviceURL + "SecurityService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
         
         final QName stsServiceName = new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTS");
         final QName stsPortName = new QName("urn:picketlink:identity-federation:sts", "PicketLinkSTSPort");
         final URL stsURL = new URL(serviceURL.getProtocol(), serviceURL.getHost(), serviceURL.getPort(), "/jaxws-samples-wsse-policy-trustPicketLink-sts/PicketLinkSTS?wsdl");
         WSTrustTestUtils.setupWsseAndSTSClient(proxy, bus, stsURL.toString(), stsServiceName, stsPortName);
         
         try {
            assertEquals("WS-Trust Hello World!", proxy.sayHello());
         } catch (Exception e) {
            throw CryptoCheckHelper.checkAndWrapException(e);
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(BEARER_SERVER_DEP)
   @WrapThreadContextClassLoader
   public void testBearer() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);

         final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/bearerwssecuritypolicy", "BearerService");
         Service service = Service.create(new URL(serviceURL + "BearerService?wsdl"), serviceName);
         BearerIface proxy = (BearerIface) service.getPort(BearerIface.class);

         WSTrustTestUtils.setupWsseAndSTSClientBearer((BindingProvider) proxy, bus);
         assertEquals("Bearer WS-Trust Hello World!", proxy.sayHello());

      }
      finally
      {
         bus.shutdown(true);
      }
   }

   
}
