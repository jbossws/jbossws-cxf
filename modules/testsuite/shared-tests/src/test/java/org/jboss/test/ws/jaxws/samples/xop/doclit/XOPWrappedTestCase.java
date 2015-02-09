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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

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
   private WrappedEndpoint port;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      return DeploymentArchive.createDeployment("wrapped");
   }

   protected void setUp() throws Exception
   {

      QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "WrappedService");
      URL wsdlURL = new URL(baseURL + "wrapped?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(WrappedEndpoint.class);

      SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);

   }

   @Test
   @RunAsClient
   public void testParameterAnnotation() throws Exception
   {
      setUp();
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
