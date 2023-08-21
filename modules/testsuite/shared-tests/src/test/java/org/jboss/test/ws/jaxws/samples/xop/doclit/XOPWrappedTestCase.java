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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test @XmlMimeType annotations on wrapped services.
 * The annotations should be copied to the generated wrapper beans.
 * 
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class XOPWrappedTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      return DeploymentArchive.createDeployment("wrapped");
   }

   @Test
   @RunAsClient
   public void testParameterAnnotation() throws Exception
   {
      QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "WrappedService");
      URL wsdlURL = new URL(baseURL + "wrapped?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);
      
      DataHandler request = new DataHandler("Client data", "text/plain");
      DataHandler response = port.parameterAnnotation(request);

      assertNotNull(response);
      Object content = getContent(response);
      String contentType = response.getContentType();

      assertEquals("Server data", content);
      assertEquals("text/plain", contentType);      
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
