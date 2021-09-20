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
