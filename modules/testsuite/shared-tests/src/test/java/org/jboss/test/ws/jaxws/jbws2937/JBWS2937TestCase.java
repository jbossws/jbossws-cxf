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
package org.jboss.test.ws.jaxws.jbws2937;

import java.io.StringReader;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * [JBWS-2937] Cannot create dispatch object using EPR based javax.xml.ws.Service.createDispatch methods.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class JBWS2937TestCase extends JBossWSTest
{
   private static final WebServiceFeature[] ADDRESSING_ENABLED = { new AddressingFeature(true) };
   private static final WebServiceFeature[] ADDRESSING_DISABLED = { new AddressingFeature(false) };
   private static final String NAMESPACE_URI = "http://jboss.org/jbws2937";
   private static final String XML = "<ns1:echo xmlns:ns1='http://jboss.org/jbws2937'>" +
      " <arg0>" +
      "  <string>Kermit</string>" +
      "  <qname>TheFrog</qname>" +
      " </arg0>" +
      "</ns1:echo>";
   private static final QName SERVICE_QNAME = new QName(NAMESPACE_URI, "EndpointService");
   private static final QName PORT_QNAME = new QName(NAMESPACE_URI, "EndpointPort");
   private Service service;
   private Endpoint proxy;
   private EndpointReference epr;
   private UserType user;


   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name="jaxws-jbws2937", testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2937.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2937.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2937.UserType.class);
      return archive;
   }
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2937" + "?wsdl");
      this.service = EndpointService.create(wsdlURL, SERVICE_QNAME);
      this.proxy = (Endpoint)this.service.getPort(PORT_QNAME, Endpoint.class);
      this.epr = ((BindingProvider)this.proxy).getEndpointReference();
      // prepare request object
      this.user = new UserType();
      this.user.setString("Kermit");
      this.user.setQname(new QName("TheFrog"));
   }

   @Test
   @RunAsClient
   public void testProxy() throws Exception
   {
      final UserType response = this.proxy.echo(this.user);
      assertEquals(this.user, response);
   }

   @Test
   @RunAsClient
   public void testCreateDispatchUsingEPRAndSource() throws Exception
   {
      Dispatch<Source> dispatch = this.service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);

      dispatch = this.service.createDispatch(this.epr, Source.class, Service.Mode.PAYLOAD, ADDRESSING_ENABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);

      dispatch = this.service.createDispatch(this.epr, Source.class, Service.Mode.PAYLOAD, ADDRESSING_DISABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);
   }

   @Test
   @RunAsClient
   public void testCreateDispatchUsingEPRAndJAXBContext() throws Exception
   {
      Dispatch<Object> dispatch = this.service.createDispatch(PORT_QNAME, this.createJAXBContext(), Mode.PAYLOAD);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);

      dispatch = this.service.createDispatch(this.epr, this.createJAXBContext(), Service.Mode.PAYLOAD, ADDRESSING_ENABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);

      dispatch = this.service.createDispatch(this.epr, this.createJAXBContext(), Service.Mode.PAYLOAD, ADDRESSING_DISABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      this.epr = dispatch.getEndpointReference();
      printEPR(this.epr);
   }
   
   private JAXBContext createJAXBContext()
   {
      try
      {
         return JAXBContext.newInstance(ObjectFactory.class);
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   private void invokeObjectDispatch(final Dispatch<Object> dispatch) throws Exception
   {
      Echo request = new Echo();
      request.setArg0(this.user);
      EchoResponse response = (EchoResponse)dispatch.invoke(request);
      assertEquals(response.getReturn(), this.user);
   }
   
   private void invokeSourceDispatch(final Dispatch<Source> dispatch) throws Exception
   {
      Source request = new StreamSource(new StringReader(XML));
      Source response = dispatch.invoke(request);
      verifyResponse(response);
   }

   private void verifyResponse(final Source result) throws Exception
   {
      final Element echoResponseElement = DOMUtils.sourceToElement(result);
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(echoResponseElement));
      assertNotNull("echoResponse element is null", echoResponseElement);
      // validate return element
      final Element returnElement = DOMUtils.getFirstChildElement(echoResponseElement);
      assertNotNull("return element is null", returnElement);
      assertEquals("return", returnElement.getNodeName());
      // validate string element
      final Element stringElement = DOMUtils.getFirstChildElement(returnElement, "string");
      assertNotNull("string element is null", stringElement);
      assertEquals("string", stringElement.getNodeName());
      assertEquals("Kermit", stringElement.getTextContent());
      // validate string element
      final Element qnameElement = DOMUtils.getFirstChildElement(returnElement, "qname");
      assertNotNull("qname element is null", qnameElement);
      assertEquals("qname", qnameElement.getNodeName());
      assertEquals("TheFrog", qnameElement.getTextContent());
   }

   private void printEPR(final EndpointReference epr) throws Exception
   {
      DOMResult dr = new DOMResult(); 
      epr.writeTo(dr);
      Node endpointReferenceElement = dr.getNode();
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(endpointReferenceElement));
   }

   private void assertEquals(final UserType user1, final UserType user2)
   {
      assertEquals("user.string differs", user1.getString(), user2.getString());
      assertEquals("user.qname differs", user1.getQname(), user2.getQname());
   }
}
