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
package org.jboss.test.ws.jaxws.jbws1422;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1422] NPE if @WebParam.name like "mX.."
 * 
 * @author Thomas.Diesler@jboss.com 
 */
@RunWith(Arquillian.class)
public class JBWS1422TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1422.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvc.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvcImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testDeployment() throws Exception
   {
      QName serviceName = new QName("http://jbws1422.jaxws.ws.test.jboss.org/", "JBWS1422Service");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1422/JBWS1422Service/IWebsvcImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IWebsvc port = service.getPort(IWebsvc.class);
      String result = port.cancel("myFooBar");
      assertEquals("Cancelled-myFooBar", result);
   }
}
