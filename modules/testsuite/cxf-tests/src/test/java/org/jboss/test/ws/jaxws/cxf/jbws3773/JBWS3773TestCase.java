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
package org.jboss.test.ws.jaxws.cxf.jbws3773;

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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3773TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3773.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.Greeter.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.GreeterImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.TargetServlet.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testServletRequestAvailability() throws Exception
   {
      Greeter greeter = initPort();

      AddressingProperties addrProperties = new AddressingProperties();

      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue(baseURL + "/target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);

      BindingProvider provider = (BindingProvider)greeter;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);

      greeter.sayHi("Foo");
      Thread.sleep(1500);
      String result = getTargetServletResult();
      assertTrue("Expected ReplyTo:", result.startsWith("ReplyTo:"));
      assertTrue("Expected <return>http</return>:", result.indexOf("<return>http</return>") > 0);
   }
  
   private Greeter initPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/SOAPService?wsdl");
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
