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
package org.jboss.test.ws.jaxws.wrapped.accessor;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * Test different JAXB accesor types.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class AccessorTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-wrapped-accessor.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.Accessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.AccessorImpl.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessorResponse.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessorResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/wrapped/accessor/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testFieldAndMethodAccessors() throws Exception
   {
      QName serviceName = new QName("http://accessor.wrapped.jaxws.ws.test.jboss.org/", "AccessorService");
      URL wsdlURL = new URL(baseURL + "AccessorService?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Accessor proxy = (Accessor) service.getPort(Accessor.class);
      
      assertEquals("ing123", proxy.fieldAccessor("ing", 123));
      assertEquals("moretesting456", proxy.fieldAccessor("moretesting", 456));
   }
}
