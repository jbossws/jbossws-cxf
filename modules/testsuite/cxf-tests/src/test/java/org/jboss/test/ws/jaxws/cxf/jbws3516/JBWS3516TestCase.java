/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3516;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3516TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3516.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf.impl\n"))
            .addPackages(false, new Filter<ArchivePath>() {
               @Override
               public boolean include(ArchivePath object)
               {
                  return !object.get().contains("TestCase");
               }}, "org.jboss.test.ws.jaxws.cxf.jbws3516")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3516/WEB-INF/wsdl/hello_world.wsdl"), "wsdl/hello_world.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3516/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testOneWayFaultTo() throws Exception
   {
      Greeter greeter = initPort();
      AddressingProperties addrProperties = new AddressingProperties();
      EndpointReferenceType faultTo = new EndpointReferenceType();
      AttributedURIType epr = new AttributedURIType();
      String serverHost = getServerHost();
      int serverPort = getServerPort();
      epr.setValue("http://" + serverHost + ":" + serverPort + "/jaxws-cxf-jbws3516/target/faultTo");
      faultTo.setAddress(epr);
      addrProperties.setFaultTo(faultTo);

      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue("http://" + serverHost + ":" + serverPort + "/jaxws-cxf-jbws3516/target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);
      
      BindingProvider provider = (BindingProvider)greeter;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);
      
      greeter.pingMe();
      Thread.sleep(1000);
      String result = getTargetServletResult();
      assertTrue("Expected FaultTo:", result.startsWith("FaultTo:"));
      assertTrue("Expected PingMeFault:", result.indexOf("Intended PingMe Fault") > 0);
   }

   
   @Test
   @RunAsClient
   public void testRequestResponseFaultTo() throws Exception
   {
      Greeter greeter = initPort();

      AddressingProperties addrProperties = new AddressingProperties();

      EndpointReferenceType faultTo = new EndpointReferenceType();
      AttributedURIType epr = new AttributedURIType();
      String serverHost = getServerHost();
      int serverPort = getServerPort();
      epr.setValue("http://" + serverHost + ":" + serverPort + "/jaxws-cxf-jbws3516/target/faultTo");
      faultTo.setAddress(epr);
      addrProperties.setFaultTo(faultTo);

      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue("http://" + serverHost + ":" + serverPort + "/jaxws-cxf-jbws3516/target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);

      BindingProvider provider = (BindingProvider)greeter;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);

      greeter.sayHi("hello");
      Thread.sleep(1000);
      String result = getTargetServletResult();
      assertTrue("Expected Replyto:", result.startsWith("ReplyTo:"));
      assertTrue("Expected sayHiResponse:", result.indexOf("sayHiResponse") > 0);
      
      greeter.sayHi("fault");
      Thread.sleep(1000);
      result = getTargetServletResult();
      assertTrue("Expected FaultTo:", result.startsWith("FaultTo:"));
      assertTrue("Expected sayHiFault:", result.indexOf("Intended SayHi Fault") > 0);
   }
  
   private Greeter initPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/helloworld?wsdl");
      QName qname = new QName("http://jboss.org/hello_world", "SOAPService");
      Service service = Service.create(wsdlURL, qname);
      Greeter greeter = service.getPort(Greeter.class, new AddressingFeature());
      return greeter;
   }

   private String getTargetServletResult() throws Exception
   {
      URL url = new URL(baseURL + "/target/result");
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
