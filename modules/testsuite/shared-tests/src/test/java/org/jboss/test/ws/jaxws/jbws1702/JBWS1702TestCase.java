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
package org.jboss.test.ws.jaxws.jbws1702;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassB;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassC;
import org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperB;
import org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperC;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1702] JAXWS type inheritance
 * 
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1702TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1702.war");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1702.JBWS1702TestCase.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSBareSEI.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSRpcSEI.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSWithDocument_Bare.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSWithDocument_Wrapped.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSWithRPC_Bare.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.SampleWSWrappedSEI.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.types.ClassA.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.types.ClassB.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.types.ClassC.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperB.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperC.class)
            .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws1702/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1702/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testInheritanceRpc() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "SampleWSWithRPC_Bare?wsdl");
      QName serviceName = new QName("http://jbws1702.jaxws.ws.test.jboss.org/", "SampleWSWithRPC_BareService");
      Service service = Service.create(wsdlURL, serviceName);

      SampleWSRpcSEI port = service.getPort(SampleWSRpcSEI.class);
      ClassB b = port.getClassCAsClassB();      
      assertTrue("Should be an instance of ClassC, but was " + b, (b instanceof ClassC));
   }

   @Test
   @RunAsClient
   public void testInheritanceBare() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "SampleWSWithDocument_Bare?wsdl");
      QName serviceName = new QName("http://jbws1702.jaxws.ws.test.jboss.org/", "SampleWSWithDocument_BareService");
      Service service = Service.create(wsdlURL, serviceName);

      SampleWSBareSEI port = service.getPort(SampleWSBareSEI.class);
      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "getClassCAsClassB");
      ResponseWrapperB wrapper = port.getClassCAsClassB();
      ClassB b = wrapper.getData();
      assertTrue("Should be an instance of ClassC, but was " + b, (b instanceof ClassC));
      bp.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "getClassC");
      ResponseWrapperC wrapperC = port.getClassC();
      assertNotNull(wrapperC.getData()); 
   }

   @Test
   @RunAsClient
   public void testInheritanceWrapped() throws Exception
   {      
      URL wsdlURL = new URL(baseURL + "SampleWSWithDocument_Wrapped?wsdl");
      QName serviceName = new QName("http://jbws1702.jaxws.ws.test.jboss.org/", "SampleWSWithDocument_WrappedService");
      Service service = Service.create(wsdlURL, serviceName);

      SampleWSWrappedSEI port = service.getPort(SampleWSWrappedSEI.class);
      ClassB b = port.getClassCAsClassB();
      assertTrue("Should be an instance of ClassC, but was " + b, (b instanceof ClassC));
   }

}
