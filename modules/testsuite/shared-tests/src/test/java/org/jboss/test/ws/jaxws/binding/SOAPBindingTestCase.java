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

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP12 binding type
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class SOAPBindingTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-binding";

   public static Test suite()
   {
      return new JBossWSTestSetup(SOAPBindingTestCase.class, "jaxws-binding.war");
   }

   // [JBWS-1761] - WSProvide ignores SOAPBinding declaration
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");

      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdl = wsdlReader.readWSDL(wsdlURL.toString());
      
      String port = "SOAPEndpointPort";
      QName serviceQName = new QName("http://org.jboss.ws/jaxws/binding", "SOAPEndpointService");
      Binding wsdlBinding = wsdl.getService(serviceQName).getPort(port).getBinding();
      assertNotNull("Cannot find binding for port: " + port, wsdlBinding);

      String transport = null;
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

   public void testClientAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/binding", "SOAPEndpointService");
      Service service = Service.create(wsdlURL, qname);
      SOAPEndpoint port = (SOAPEndpoint)service.getPort(SOAPEndpoint.class);

      BindingProvider provider = (BindingProvider)port;
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new ClientHandler());
      handlerChain.add(new ClientHandler2());
      provider.getBinding().setHandlerChain(handlerChain);

      String nsURI = port.namespace();
      assertEquals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + ":" + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, nsURI);
   }

}
