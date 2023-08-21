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
package org.jboss.test.ws.jaxws.jbws2000;

import java.io.File;
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.ws.jaxws.samples.xop.doclit.GeneratorDataSource;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class JBWS2000TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2000.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2000.FileTransferService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2000.FileTransferServiceImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2000/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testFileTransfer() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2000/FileTransfer?wsdl");
      QName serviceName = new QName("http://service.mtom.test.net/", "FileTransferServiceImplService");
      Service service = Service.create(wsdlURL, serviceName);
      FileTransferService port = service.getPort(FileTransferService.class);

      SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);
      
      GeneratorDataSource source = new GeneratorDataSource(1024 * 1204 * 8); //avoid going beyond Undertow default max post size
      DataHandler dh = new DataHandler(source);

      boolean success = port.transferFile("JBWS2000.data", dh);
      assertTrue("Failed to transfer file", success);
   }

}
