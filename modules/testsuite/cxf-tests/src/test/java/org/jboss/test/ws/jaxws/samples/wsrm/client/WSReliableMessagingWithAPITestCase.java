/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsrm.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.rm.feature.RMFeature;
import org.apache.cxf.ws.rm.manager.AcksPolicyType;
import org.apache.cxf.ws.rm.manager.DestinationPolicyType;
import org.apache.cxf.ws.rmp.v200502.RMAssertion;
import org.apache.cxf.ws.rmp.v200502.RMAssertion.AcknowledgementInterval;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.wsrm.generated.SimpleService;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Client invoking web service with WS-RM and using no xml descriptor
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Aug-2010
 * 
 */
@RunWith(Arquillian.class)
public final class WSReliableMessagingWithAPITestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsrm-api.war");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.RMCheckInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.SimpleServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Echo.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.EchoResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Ping.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl"), "wsdl/SimpleService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/web.xml"));
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jjaxws-samples-wsrm-api-client.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.wsrm.client.CustomRMFeature.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
         URL wsdlURL = getResourceURL("jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         SimpleService proxy = (SimpleService)service.getPort(SimpleService.class);
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/jaxws-samples-wsrm-api/SimpleService");
         
         assertEquals("Hello World!", proxy.echo("Hello World!")); // request response call
         proxy.ping(); // one way call
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testWithFeature() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
         URL wsdlURL = getResourceURL("jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         
         RMFeature feature = new RMFeature();
         RMAssertion rma = new RMAssertion();
         RMAssertion.BaseRetransmissionInterval bri = new RMAssertion.BaseRetransmissionInterval();
         bri.setMilliseconds(4000L);
         rma.setBaseRetransmissionInterval(bri);
         AcknowledgementInterval ai = new AcknowledgementInterval();
         ai.setMilliseconds(2000L);
         rma.setAcknowledgementInterval(ai);
         feature.setRMAssertion(rma);
         DestinationPolicyType dp = new DestinationPolicyType();
         AcksPolicyType ap = new AcksPolicyType();
         ap.setIntraMessageThreshold(0);
         dp.setAcksPolicy(ap);
         feature.setDestinationPolicy(dp);
         
         SimpleService proxy = (SimpleService)service.getPort(SimpleService.class, feature);
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/jaxws-samples-wsrm-api/SimpleService");
         
         assertEquals("Hello World!", proxy.echo("Hello World!")); // request response call
         proxy.ping(); // one way call
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testWithFeatureProperty() throws Exception
   {
      final Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
         URL wsdlURL = getResourceURL("jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         SimpleService proxy = (SimpleService)service.getPort(SimpleService.class);
         
         ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
         configurer.setConfigProperties(proxy, "META-INF/jaxws-client-config.xml", "Custom Client Config");
         
         ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/jaxws-samples-wsrm-api/SimpleService");
         
         assertEquals("Hello World!", proxy.echo("Hello World!")); // request response call
         proxy.ping(); // one way call
      } finally {
         bus.shutdown(true);
      }
   }
   
}
