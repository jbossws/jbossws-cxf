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
package org.jboss.test.ws.jaxws.jbws2259;

import java.io.File;
import java.net.URL;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
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
 * Test case to test MTOM detection.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 30th March 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2259
 */
@RunWith(Arquillian.class)
public class JBWS2259TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2259.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2259.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.Photo.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws2259", "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      SOAPBinding soapBinding = (SOAPBinding)bindingProvider.getBinding();
      soapBinding.setMTOMEnabled(true);
      
      File sharkFile = getResourceFile("jaxws/jbws2259/attach.jpeg");
      DataSource ds = new FileDataSource(sharkFile);
      DataHandler handler = new DataHandler(ds);

      String expectedContentType = "image/jpeg";

      Photo p = new Photo();
      p.setCaption("JBWS2259 Smile :-)");
      p.setExpectedContentType(expectedContentType);
      p.setImage(handler);

      Photo reponse = port.echo(p);
      DataHandler dhResponse = reponse.getImage();

      String contentType = dhResponse.getContentType();
      assertEquals("content-type", expectedContentType, contentType);
   }

}
