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
package org.jboss.test.ws.jaxws.binding;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test SOAP12 binding type
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
@RunWith(Arquillian.class)
public class SOAPBindingTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-binding.war");
      archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.binding.SOAPEndpoint.class)
            .addClass(org.jboss.test.ws.jaxws.binding.SOAPEndpointBean.class)
            .addClass(org.jboss.test.ws.jaxws.binding.ServerHandler.class)
            .addAsResource("org/jboss/test/ws/jaxws/binding/jaxws-server-handlers.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/binding/WEB-INF/web.xml"));
      return archive;
   }   
   // [JBWS-1761] - WSProvide ignores SOAPBinding declaration
   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");

      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdl = wsdlReader.readWSDL(wsdlURL.toString());

      String port = "SOAPEndpointPort";
      QName serviceQName = new QName("http://org.jboss.ws/jaxws/binding", "SOAPEndpointService");
      Binding wsdlBinding = wsdl.getService(serviceQName).getPort(port).getBinding();
      assertNotNull("Cannot find binding for port: " + port, wsdlBinding);

      String transport = null;
      @SuppressWarnings("unchecked")
      List<ExtensibilityElement> extList = wsdlBinding.getExtensibilityElements();
      for (ExtensibilityElement ext : extList)
      {
         if (ext instanceof SOAPBinding)
         {
            fail("Expected SOAP-1.2 binding");
         }
         else if (ext instanceof SOAP12Binding)
         {
            SOAP12Binding soapBinding = (SOAP12Binding)ext;
            transport = soapBinding.getTransportURI();
         }
      }
      if (isIntegrationCXF())
      {
         System.out.println("FIXME: [CXF-2531] Wrong \"transport\" attribute in soap12:binding");
      }
      else
      {
         assertEquals("Invalid transport uri", "http://schemas.xmlsoap.org/soap/http", transport);
      }
   }
   @Test
   @RunAsClient
   public void testClientAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/binding", "SOAPEndpointService");
      Service service = Service.create(wsdlURL, qname);
      SOAPEndpoint port = service.getPort(SOAPEndpoint.class);

      BindingProvider provider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new ClientHandler());
      handlerChain.add(new ClientHandler2());
      provider.getBinding().setHandlerChain(handlerChain);

      String nsURI = port.namespace();
      assertEquals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + ":" + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, nsURI);
   }

}
