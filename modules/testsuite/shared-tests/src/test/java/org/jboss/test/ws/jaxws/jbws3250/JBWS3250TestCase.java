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
package org.jboss.test.ws.jaxws.jbws3250;

import java.io.File;
import java.net.URL;

import jakarta.activation.DataHandler;
import jakarta.activation.URLDataSource;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3250TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3250.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3250.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.MTOMRequest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.MTOMResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3250/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testMtomSawpFile() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws3250", "TestEndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      SOAPBinding binding =(SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);
      URL url = JBossWSTestHelper.getResourceURL("jaxws/jbws3250/wsf.png");
      URLDataSource urlDatasource = new URLDataSource(url);
      jakarta.activation.DataHandler dh = new DataHandler(urlDatasource);
      MTOMRequest request = new MTOMRequest();
      request.setContent(dh);
      request.setId("largeSize_mtom_request");
      MTOMResponse mtomResponse = port.echo(request);
      Assert.assertEquals("Response for requestID:largeSize_mtom_request", mtomResponse.getResponse());
      byte[] responseBytes = IOUtils.convertToBytes(mtomResponse.getContent());
      Assert.assertTrue(responseBytes.length > 65536);
   }

}
