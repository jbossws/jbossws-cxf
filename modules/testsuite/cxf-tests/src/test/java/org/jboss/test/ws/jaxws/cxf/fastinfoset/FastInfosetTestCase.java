/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.fastinfoset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class FastInfosetTestCase extends JBossWSTest
{
   public static final String DEP1 = "dep1";
   public static final String DEP2 = "dep2";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false, name = DEP1)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-fastinfoset.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.fastinfoset.HelloWorldImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.fastinfoset.HelloWorldFIImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.fastinfoset.HelloWorldFeatureImpl.class)
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/fastinfoset/WEB-INF/jaxws-endpoint-config.xml")), "jaxws-endpoint-config.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/fastinfoset/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(testable = false, name = DEP2)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-fastinfoset2.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/fastinfoset/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
            .addClass(org.jboss.test.ws.jaxws.cxf.fastinfoset.HelloWorldImpl.class);
      return archive;
   }

   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-fastinfoset-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/fastinfoset/META-INF-client/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP1)
   public void testInfosetUsingFastInfosetAnnotation() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream in = new ByteArrayOutputStream();
      PrintWriter pwIn = new PrintWriter(in);
      PrintWriter pwOut = new PrintWriter(out);
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pwIn));
         bus.getOutInterceptors().add(new LoggingOutInterceptor(pwOut));
   
         URL wsdlURL = new URL(baseURL + "HelloWorldService/HelloWorldFIImpl?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldFIService");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldFIImplPort");
         HelloWorldFI port = (HelloWorldFI) service.getPort(portQName, HelloWorldFI.class);
         assertEquals("helloworld", port.echo("helloworld"));
         assertTrue("request is expected fastinfoset", out.toString().indexOf("application/fastinfoset") > -1);
         assertTrue("response is expected fastinfoset", in.toString().indexOf("application/fastinfoset") > -1);
      } finally {
         bus.shutdown(true);
         pwOut.close();
         pwIn.close();
      }
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP1)
   public void testInfosetUsingFeature() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream in = new ByteArrayOutputStream();
      PrintWriter pwIn = new PrintWriter(in);
      PrintWriter pwOut = new PrintWriter(out);
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pwIn));
         bus.getOutInterceptors().add(new LoggingOutInterceptor(pwOut));
   
         URL wsdlURL = new URL(baseURL + "HelloWorldService/HelloWorldFeatureImpl?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldFeatureService");
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldFeatureImplPort");
         HelloWorldFeature port = (HelloWorldFeature) service.getPort(portQName, HelloWorldFeature.class);
         assertEquals("helloworldFeature", port.echo("helloworldFeature"));
         assertTrue("request is expected fastinfoset", out.toString().indexOf("application/fastinfoset") > -1);
         assertTrue("response is expected fastinfoset", in.toString().indexOf("application/fastinfoset") > -1);
      } finally {
         bus.shutdown(true);
         pwOut.close();
         pwIn.close();
      }
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(DEP1)
   public void testInfosetUsingFeatureProperties() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "HelloWorldService/HelloWorldImpl?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldService");
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldImplPort");
      internalTestInfosetUsingFeatureProperties(wsdlURL, serviceName, portQName);
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   @OperateOnDeployment(DEP2)
   public void testInfosetUsingFeatureProperties2() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "HelloWorldService?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldService");
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldImplPort");
      internalTestInfosetUsingFeatureProperties(wsdlURL, serviceName, portQName);
   }
   
   private void internalTestInfosetUsingFeatureProperties(URL wsdlURL, QName serviceName, QName portQName) throws Exception {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream in = new ByteArrayOutputStream();
      PrintWriter pwIn = new PrintWriter(in);
      PrintWriter pwOut = new PrintWriter(out);
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pwIn));
         bus.getOutInterceptors().add(new LoggingOutInterceptor(pwOut));
   
         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         HelloWorld port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
         
         ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
         configurer.setConfigProperties(port, "META-INF/jaxws-client-config.xml", "Custom Client Config");
         
         assertEquals("helloworld", port.echo("helloworld"));
         assertTrue("request is expected fastinfoset", out.toString().indexOf("application/fastinfoset") > -1);
         assertTrue("response is expected fastinfoset", in.toString().indexOf("application/fastinfoset") > -1);
      } finally {
         bus.shutdown(true);
         pwOut.close();
         pwIn.close();
      }
   }
   
}
