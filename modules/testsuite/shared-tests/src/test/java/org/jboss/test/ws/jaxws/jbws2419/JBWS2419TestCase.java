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
package org.jboss.test.ws.jaxws.jbws2419;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.SOAPBinding;

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
 * Test SOAP 1.2 and SOAP 1.1 MTOM/XOP request/response content type and start-info
 *
 * @author mageshbk@jboss.com
 * @since 20-Feb-2009
 */
@RunWith(Arquillian.class)
public class JBWS2419TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2419.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP11Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP11EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP11ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP12Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP12EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2419.SOAP12ServerHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws2419/jaxws-server-handlers1.xml")
               .addAsResource("org/jboss/test/ws/jaxws/jbws2419/jaxws-server-handlers2.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2419/WEB-INF/wsdl/SOAP12Service.wsdl"), "wsdl/SOAP12Service.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2419/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSOAP12ClientAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/soap12?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws2419", "SOAP12EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      SOAP12Endpoint port = service.getPort(SOAP12Endpoint.class);

      BindingProvider provider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new SOAP12ClientHandler());
      provider.getBinding().setHandlerChain(handlerChain);
      ((SOAPBinding)provider.getBinding()).setMTOMEnabled(true);

      DataHandler response = port.namespace(new DataHandler("Jimbo","text/plain"));
      Object messg = getContent(response);
      assertEquals("Hello Jimbo", messg);
   }

   @Test
   @RunAsClient
   public void testSOAP11ClientAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/soap11?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws2419", "SOAP11EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      SOAP11Endpoint port = service.getPort(SOAP11Endpoint.class);

      BindingProvider provider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new SOAP11ClientHandler());
      provider.getBinding().setHandlerChain(handlerChain);
      ((SOAPBinding)provider.getBinding()).setMTOMEnabled(true);

      DataHandler response = port.namespace(new DataHandler("Jimbo","text/plain"));
      Object messg = getContent(response);
      assertEquals("Hello Jimbo", messg);
   }

   protected Object getContent(DataHandler dh) throws IOException
   {
      Object content = dh.getContent();

      // Metro returns an ByteArrayInputStream
      if (content instanceof InputStream)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
            return br.readLine();
         }
         finally
         {
            ((InputStream)content).close();
         }
      }
      return content;
   }
}
