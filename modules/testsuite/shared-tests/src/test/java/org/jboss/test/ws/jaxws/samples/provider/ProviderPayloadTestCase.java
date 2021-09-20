/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.provider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
@RunWith(Arquillian.class)
public class ProviderPayloadTestCase extends JBossWSTest
{
   private final String reqString =
      "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'>Hello</ns1:somePayload>";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-provider-payload.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.provider.LogicalSourceHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.provider.ProviderBeanPayload.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/provider/provider-handlers.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/provider/payload/WEB-INF/wsdl/Provider.wsdl"), "wsdl/Provider.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/provider/payload/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testProviderDispatch() throws Exception
   {
      Dispatch<Source> dispatch = createDispatch("ProviderEndpoint");
      Source resPayload = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));

      verifyResponse(resPayload);
   }

   private void verifyResponse(Source xml) throws IOException
   {
      Element was = DOMUtils.sourceToElement(xml);

      if(!"somePayload".equals(was.getLocalName())
        || !"http://org.jboss.ws/provider".equals(was.getNamespaceURI())
        || !"Hello:Inbound:LogicalSourceHandler:Outbound:LogicalSourceHandler".equals( DOMUtils.getTextContent(was)))
      {
         throw new WebServiceException("Unexpected payload: " + xml);
      }
   }

   @Test
   @RunAsClient
   public void testProviderMessage() throws Exception
   {
      String reqEnvStr =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Body>" + reqString + "</env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnvStr.getBytes()));

      URL epURL = baseURL;
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();

      SOAPHeader soapHeader = resEnv.getHeader();
      if (soapHeader != null)
         soapHeader.detachNode();

      Node responseBody = DOMUtils.getFirstChildElement(resEnv.getBody());
      assertEquals("wrong namespace: " + responseBody.getNamespaceURI(), "http://org.jboss.ws/provider", responseBody.getNamespaceURI());
      assertEquals("wrong localPart: " + responseBody.getLocalName(), "somePayload", responseBody.getLocalName());
      String responseString = DOMUtils.getTextContent(responseBody);
      assertEquals("wrong content: " + responseString, "Hello:Inbound:LogicalSourceHandler:Outbound:LogicalSourceHandler", responseString);
   }

   private Dispatch<Source> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/provider";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL(baseURL + "/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress.toExternalForm());

      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      return dispatch;
   }
}
