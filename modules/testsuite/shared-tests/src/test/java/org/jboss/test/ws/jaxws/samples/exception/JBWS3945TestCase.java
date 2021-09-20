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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.exception.client.ExceptionEndpoint;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * [JBWS-3945] Set CXF-6198 property and get SoapFaults for HTTP 400 errors
 *
 * @author <a href="alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class JBWS3945TestCase extends JBossWSTest
{
   private String targetNS = "http://server.exception.samples.jaxws.ws.test.jboss.org/";
   private String targetEndpoint = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-exception-jbws3945/SOAP12ExceptionEndpointService";
   
   @Deployment(name="jaxws-samples-exception-jbws3945", testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-jbws3945.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.JBWS3945EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.JBWS3945Servlet.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.UserException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.UserExceptionBean.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/WEB-INF/web-3945.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testSOAP12SoapFaultException() throws Exception
   {
      try
      {
         QName serviceName = new QName(targetNS, "JBWS3945EndpointImplService");
         Service service = Service.create(new URL(targetEndpoint + "?wsdl"), serviceName);
         ExceptionEndpoint ep = service.getPort(ExceptionEndpoint.class);
         
         testSoapFaultException(ep);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
   
   @Test
   @RunAsClient
   public void testSOAP12SoapFaultExceptionOnHTTP400() throws Exception
   {
      try
      {
         QName serviceName = new QName(targetNS, "JBWS3945EndpointImplService");
         Service service = Service.create(new URL(targetEndpoint + "?wsdl"), serviceName);
         ExceptionEndpoint ep = service.getPort(ExceptionEndpoint.class);
         ((BindingProvider)ep).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, targetEndpoint + "Servlet");
         
         testSoapFaultException(ep);
      }
      catch (WebServiceException wse)
      {
         wse.printStackTrace();
         assertTrue(wse.getCause().getMessage().contains("400: Bad Request"));
      }
      catch (Exception e)
      {
         fail(e);
      }
      
      try
      {
         QName serviceName = new QName(targetNS, "JBWS3945EndpointImplService");
         Service service = Service.create(new URL(targetEndpoint + "?wsdl"), serviceName);
         ExceptionEndpoint ep = service.getPort(ExceptionEndpoint.class);
         ((BindingProvider)ep).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, targetEndpoint + "Servlet");
         ((BindingProvider)ep).getRequestContext().put("org.apache.cxf.transport.process_fault_on_http_400", true);
         
         testSoapFaultException(ep);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
   
   @Test
   @RunAsClient
   public void testSOAP12SoapFaultExceptionOnHTTP400UsingSAAJ() throws Exception
   {
      try
      {
         //<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"><soap:Body><ns2:throwSoapFaultException xmlns:ns2="http://server.exception.samples.jaxws.ws.test.jboss.org/"/></soap:Body></soap:Envelope>
         
         SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();

         SOAPConnection con = conFac.createConnection();
         MessageFactory msgFactory = MessageFactory.newInstance();
         SOAPMessage msg = msgFactory.createMessage();
         msg.getSOAPBody().addBodyElement(new QName(targetNS, "throwSoapFaultException"));
         SOAPMessage response = con.call(msg, new URL(targetEndpoint + "Servlet"));
         Element el = (Element)response.getSOAPBody().getChildElements().next();
         assertEquals("Fault", el.getLocalName());
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
   
   private void testSoapFaultException(ExceptionEndpoint ep) throws Exception
   {
      try
      {
         ep.throwSoapFaultException();
         fail("Expected SOAPFaultException");
      }
      catch (SOAPFaultException e)
      {
         assertEquals("this is a fault string!", e.getFault().getFaultString());
         assertEquals("mr.actor", e.getFault().getFaultActor());
         assertEquals("Sender", e.getFault().getFaultCodeAsName().getLocalName());
         assertEquals("http://www.w3.org/2003/05/soap-envelope", e.getFault().getFaultCodeAsName().getURI());
         final QName subcode = (QName)e.getFault().getFaultSubcodes().next();
         assertEquals("http://ws.gss.redhat.com/", subcode.getNamespaceURI());
         assertEquals("AnException", subcode.getLocalPart());
         assertEquals("test", ((Element)e.getFault().getDetail().getChildElements().next()).getLocalName());
         assertEquals("it", e.getFault().getFaultReasonLocales().next().toString());
      }
   }

   private static void fail(Exception e) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(bos);
      e.printStackTrace(ps);
      fail(bos.toString());
      ps.close();
   }
}
