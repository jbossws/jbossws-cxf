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
package org.jboss.test.ws.jaxws.jbws2419;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP 1.2 and SOAP 1.1 MTOM/XOP request/response content type and start-info
 *
 * @author mageshbk@jboss.com
 * @since 20-Feb-2009
 */
public class JBWS2419TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2419";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2419TestCase.class, "jaxws-jbws2419.war");
   }

   public void testSOAP12ClientAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "/soap12?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws2419", "SOAP12EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      SOAP12Endpoint port = (SOAP12Endpoint)service.getPort(SOAP12Endpoint.class);

      BindingProvider provider = (BindingProvider)port;
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new SOAP12ClientHandler());
      provider.getBinding().setHandlerChain(handlerChain);
      ((SOAPBinding)provider.getBinding()).setMTOMEnabled(true);

      DataHandler response = port.namespace(new DataHandler("Jimbo","text/plain"));
      Object messg = getContent(response);
      assertEquals("Hello Jimbo", messg);
   }

   public void testSOAP11ClientAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "/soap11?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws2419", "SOAP11EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      SOAP11Endpoint port = (SOAP11Endpoint)service.getPort(SOAP11Endpoint.class);

      BindingProvider provider = (BindingProvider)port;
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
