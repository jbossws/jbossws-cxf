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
package org.jboss.test.ws.jaxws.endpointReference;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.Test;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test EPR related methods
 *
 * @author alessio.soldano@jboss.com
 * @author ropalka@redhat.com
 * @since 13-Jan-2009
 */
public class EndpointReferenceTestCase extends JBossWSTest
{
   private static final String ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-endpointReference";
   private static final String ENDPOINT_WSDL = ENDPOINT_ADDRESS + "?wsdl";
   private static final String WSDL_NS = "http://org.jboss.ws/endpointReference";
   private static final QName SERVICE_QNAME = new QName(WSDL_NS, "EndpointService");
   private static final QName PORT_QNAME = new QName(WSDL_NS, "EndpointPort");

   private Service service;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-endpointReference.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.endpointReference.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.endpointReference.EndpointImpl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(EndpointReferenceTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void setUp() throws Exception
   {
      this.service = Service.create(new URL(ENDPOINT_WSDL), SERVICE_QNAME);
   }

   public void testDispatch() throws Exception
   {
      final Dispatch<Source> dispatch = this.service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD);
      this.validateEndpointReferences(dispatch);
   }

   public void testDispatchWithFeatures() throws Exception
   {
      final Dispatch<Source> dispatch = this.service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD, new AddressingFeature(false, false));
      this.validateEndpointReferences(dispatch);
   }

   public void testPort() throws Exception
   {
      final Endpoint port = this.service.getPort(Endpoint.class);
      this.validateEndpointReferences((BindingProvider)port);
   }

   private void validateEndpointReferences(final BindingProvider bp) throws Exception
   {
      assertEndpointReference(bp.getEndpointReference());
      assertEndpointReference(bp.getEndpointReference(W3CEndpointReference.class));
      try
      {
         bp.getEndpointReference(MyEndpointReference.class);
         fail("WebServiceException expected");
      }
      catch (WebServiceException e)
      {
         //NOP: the provided EndpointReference is not supported by the implementation
      }
      catch (Throwable t)
      {
         fail("WebServiceException expected, got " + t);
      }

      Endpoint port = bp.getEndpointReference().getPort(Endpoint.class);
      assertNotNull(port);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);

      port = bp.getEndpointReference(W3CEndpointReference.class).getPort(Endpoint.class);
      assertNotNull(port);
      retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   private void assertEndpointReference(EndpointReference epr) throws Exception
   {
      assertEquals(W3CEndpointReference.class.getName(), epr.getClass().getName());
      Element endpointReference = DOMUtils.parse(epr.toString());
      assertEquals("EndpointReference", endpointReference.getNodeName());
      assertEquals("http://www.w3.org/2005/08/addressing", endpointReference.getAttribute("xmlns"));
      NodeList addresses = endpointReference.getElementsByTagName("Address");
      assertEquals(1, addresses.getLength());
      String eprAddress = addresses.item(0).getFirstChild().getNodeValue();
      eprAddress = eprAddress.replace("127.0.0.1", "localhost");
      assertEquals(ENDPOINT_ADDRESS.replace("127.0.0.1", "localhost"), eprAddress);
   }

   private static class MyEndpointReference extends EndpointReference
   {
      @Override
      public void writeTo(Result result)
      {
         throw new UnsupportedOperationException();
      }
   }
}
