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
package org.jboss.test.ws.jaxws.jbws3131;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3131TestCase extends JBossWSTest
{
   private Service service;
   private Service serviceChanged;

   @Before
   public void setUp() throws IOException
   {
      QName serviceName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2");
      service = Service.create(getResourceURL("jaxws/jbws3131/NfeStatusServico2.wsdl"), serviceName);
      serviceChanged = Service.create(getResourceURL("jaxws/jbws3131/NfeStatusServico21.wsdl"), serviceName);
   }

   @Test
   @RunAsClient
   public void testSOAP11OnOriginalWSDL() throws IOException
   {
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap");
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP12OnOriginalWSDL() throws IOException
   {
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap12");
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP11OnChangedWSDL() throws IOException
   {
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap1");
      Dispatch<Source> dispatch = serviceChanged.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", bindingID);
   }

   @Test
   @RunAsClient
   public void testSOAP12OnChangedWSDL() throws IOException
   {
      QName portName = new QName("http://www.portalfiscal.inf.br/nfe/wsdl/NfeStatusServico2", "NfeStatusServico2Soap12");
      Dispatch<Source> dispatch = serviceChanged.createDispatch(portName, Source.class, Mode.MESSAGE);
      String bindingID = dispatch.getBinding().getBindingID();
      assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/", bindingID);
   }
}