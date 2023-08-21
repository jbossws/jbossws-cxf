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
package org.jboss.test.ws.jaxws.jbws3026;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3026] Injecting EJB into Webservice via @EJB(mappedName="MyBean/remote")
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class JBWS3026TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name = "dep-jar", testable = false, order=1)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws3026-ejb.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanLocal.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote.class);
      return archive;
   }

   @Deployment(name = "dep-war", testable = false, order=2)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3026-web.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyServiceImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3026/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3026/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("dep-war")
   public void testUsecase1WithoutSar() throws Exception
   {
      String endpointAddress = baseURL + "MyService";
      QName serviceName = new QName("http://jbws3026.jaxws.ws.test.jboss.org/", "MyService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      MyService port = (MyService)service.getPort(MyService.class);
      port.useBean();
      port.thisOneWorks();
   }
}
