/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3516;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature;

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
