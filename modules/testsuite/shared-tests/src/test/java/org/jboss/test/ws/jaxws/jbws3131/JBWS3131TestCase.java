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
package org.jboss.test.ws.jaxws.jbws3131;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

@RunWith(Arquillian.class)
public class JBWS3131TestCase extends JBossWSTest
{
   private URL WSDLUrl;
   private URL changedWSDLUrl;
   private Service service;
   private Service serviceChanged;

   public void setUp() throws IOException
   {
      WSDLUrl = getResourceURL("jaxws/jbws3131/NfeStatusServico2.wsdl");
      changedWSDLUrl = getResourceURL("jaxws/jbws3131/NfeStatusServico21.wsdl");
      
      QName serviceName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2");
      service = Service.create(WSDLUrl, serviceName);

      serviceName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2");
      serviceChanged = Service.create(changedWSDLUrl, serviceName);
   }

   @Test
   @RunAsClient
   public void testSOAP11OnOriginalWSDL() throws IOException
   {
      setUp();
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap");
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP12OnOriginalWSDL() throws IOException
   {
      setUp();
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap12");
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP11OnChangedWSDL() throws IOException
   {
      setUp();
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap1");
      Dispatch<Source> dispatch = serviceChanged.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP12OnChangedWSDL() throws IOException
   {
      setUp();
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap12");
      Dispatch<Source> dispatch = serviceChanged.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", bindingID);
   }
}