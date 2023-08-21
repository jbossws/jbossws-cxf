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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
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
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 18-Jun-2013
 */
@RunWith(Arquillian.class)
public class JBWS3593TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3593.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3593.EndpointBean.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3593.MTOMOutInterceptor.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testMTOMAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/EndpointBean?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/jbws3593", "EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = service.getPort(Endpoint.class);

      addClientHandler((BindingProvider)port, true);
      
      ((SOAPBinding)((BindingProvider)port).getBinding()).setMTOMEnabled(true);

      DataHandler response = port.namespace(new DataHandler("Jimbo","text/plain"));
      Object messg = getContent(response);
      assertEquals("Hello Jimbo", messg);
   }

   @Test
   @RunAsClient
   public void testTextXmlAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/EndpointBean?wsdl");
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
