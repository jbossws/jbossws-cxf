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
package org.jboss.test.ws.saaj.jbws3084;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.saaj.SOAPConnectionFactoryImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3084] Enable control of chunked encoding when using SOAPConnection.
 *
 * @author sberyozk@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3084CxfTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "cxf-saaj-soap-connection.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.saaj.jbws3084.InputStreamDataSource.class)
            .addClass(org.jboss.test.ws.saaj.jbws3084.ServiceIface.class)
            .addClass(org.jboss.test.ws.saaj.jbws3084.ServiceImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/wsdl/SaajService.wsdl"), "wsdl/SaajService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSoapConnectionFactoryType() throws Exception
   {
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();
      assertEquals(SOAPConnectionFactoryImpl.class.getName(), conFac.getClass().getName());
   }

   @Test
   @RunAsClient
   public void testSoapConnectionGet() throws Exception
   {
      final String serviceURL = baseURL + "/greetMe";
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();

      SOAPConnection con = conFac.createConnection();
      URL endpoint = new URL(serviceURL);
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage msg = msgFactory.createMessage();
      msg.getSOAPBody().addBodyElement(new QName("http://www.jboss.org/jbossws/saaj", "greetMe"));
      SOAPMessage response = con.call(msg, endpoint);
      QName greetMeResp = new QName("http://www.jboss.org/jbossws/saaj", "greetMeResponse");

      Iterator<?> sayHiRespIterator = response.getSOAPBody().getChildElements(greetMeResp);
      SOAPElement soapElement = (SOAPElement) sayHiRespIterator.next();
      assertNotNull(soapElement);

      assertEquals(1, response.countAttachments());
   }
}
