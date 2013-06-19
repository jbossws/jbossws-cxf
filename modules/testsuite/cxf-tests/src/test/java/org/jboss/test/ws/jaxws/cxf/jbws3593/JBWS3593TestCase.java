/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 18-Jun-2013
 */
public class JBWS3593TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3593/EndpointBean";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3593TestCase.class, "jaxws-cxf-jbws3593.war");
   }

   public void testMTOMAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws3593", "EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = service.getPort(Endpoint.class);

      addClientHandler((BindingProvider)port, true);
      
      ((SOAPBinding)((BindingProvider)port).getBinding()).setMTOMEnabled(true);

      DataHandler response = port.namespace(new DataHandler("Jimbo","text/plain"));
      Object messg = getContent(response);
      assertEquals("Hello Jimbo", messg);
   }

   public void testTextXmlAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws3593", "EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = service.getPort(Endpoint.class);
      
      addClientHandler((BindingProvider)port, false);

      List<String> list = new LinkedList<String>();
      list.add("Hello");
      list.add("Jimbo");
      List<String> response = port.echoStrings(list);
      assertEquals("Hello", response.get(0));
      assertEquals("Jimbo", response.get(1));
   }
   
   @SuppressWarnings("rawtypes")
   private void addClientHandler(BindingProvider provider, boolean checkMtom) {
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new ClientHandler(checkMtom));
      provider.getBinding().setHandlerChain(handlerChain);
   }

   protected Object getContent(DataHandler dh) throws IOException
   {
      Object content = dh.getContent();

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
