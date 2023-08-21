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
package org.jboss.test.ws.jaxws.jbws2978;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JBWS2978TestCase.
 *
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
@Ignore(value="Tests migrated from JBossWS-Native specific testsuite which are meant to pass with JBossWS-CXF too, but are still to be fixed")
@RunWith(Arquillian.class)
public class JBWS2978TestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2978.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2978.AddNumbers.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2978.AddNumbersImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2978.AddNumbersRequest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2978.AddNumbersResponse.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2978/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2978/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      String text = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2978";
      String requestMessage = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Header><To xmlns='http://www.w3.org/2005/08/addressing'>"
         + text
         + "</To><Action xmlns='http://www.w3.org/2005/08/addressing'>inputAction</Action>"
         + "<MessageID xmlns='http://www.w3.org/2005/08/addressing'>uuid:56d586f8-980c-48cf-982d-77a2f56e5c5b</MessageID>"
         + "<ReplyTo xmlns='http://www.w3.org/2005/08/addressing'><Address>http://www.w3.org/2005/08/addressing/anonymous</Address></ReplyTo>"
         + "</S:Header><S:Body><ns1:addNumbers xmlns:ns1='http://ws.jboss.org'><arg0>10</arg0><arg1>10</arg1></ns1:addNumbers></S:Body></S:Envelope>";

      URL wsdlURL = new URL(text + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org", "AddNumbers");
      Service service = Service.create(wsdlURL, serviceName);
      
      try
      {
         Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName("http://ws.jboss.org", "AddNumbersPort"), SOAPMessage.class ,
               Service.Mode.MESSAGE);
         SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null,
               new ByteArrayInputStream(requestMessage.getBytes()));
         BindingProvider bp = dispatch;
         java.util.Map<String, Object> requestContext = bp.getRequestContext();
         requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, "mismatchAction");
         dispatch.invoke(reqMsg);
         fail("Should throw SOAPFaultExceptoin");
      }
      catch (SOAPFaultException e)
      {
         assertEquals(true, e.getFault().getFaultCode().indexOf("ActionMismatch") > -1);
      }
   }
}