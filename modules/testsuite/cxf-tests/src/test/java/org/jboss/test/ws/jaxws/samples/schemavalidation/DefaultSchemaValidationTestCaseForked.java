/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.schemavalidation;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.samples.schemavalidation.types.HelloResponse;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * A testcase for verifying default schema validation configured
 * through standard client/endpoint configuration (AS 7 DMR)
 * 
 * @author alessio.soldano@jboss.com
 */
public class DefaultSchemaValidationTestCaseForked extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(DefaultSchemaValidationTestCaseForked.class, DeploymentArchives.CLIENT_WAR);
   }
   
   /**
    * Verifies the default client configuration can be used to always set schema validation from AS model
    * 
    * @throws Exception
    */
   public void testDefaultClientValidation() throws Exception {
      try {
         JBossWSTestHelper.deploy(DeploymentArchives.SERVER);
         assertEquals("1", runInContainer("testDefaultClientValidation"));
      } finally {
         JBossWSTestHelper.undeploy(DeploymentArchives.SERVER);
      }
   }
   
   /**
    * Verifies the default endpoint configuration can be used to always set schema validation from AS model
    * 
    * @throws Exception
    */
   public void testDefaultServerValidation() throws Exception {
      final QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      final QName portName = new QName("http://jboss.org/schemavalidation", "HelloPort");
      URL wsdlURL = getResourceURL("jaxws/samples/schemavalidation/client.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello) service.getPort(portName, Hello.class);
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://" + getServerHost() + ":8080/jaxws-samples-schemavalidation/hello");
      HelloResponse hr;
      try {
         JBossWSTestHelper.deploy(DeploymentArchives.SERVER);
         hr = proxy.helloRequest("JBoss");
         assertNotNull(hr);
         assertEquals(2, hr.getReturn());
         hr = proxy.helloRequest("number");
         assertNotNull(hr);
         assertEquals(2, hr.getReturn());
      } finally {
         JBossWSTestHelper.undeploy(DeploymentArchives.SERVER);
      }
      
      // -- modify default conf to enable default endpoint schema validation
      try
      {
         runInContainer("enableDefaultEndpointSchemaValidation");
         try {
            JBossWSTestHelper.deploy(DeploymentArchives.SERVER);
            hr = proxy.helloRequest("JBoss");
            assertNotNull(hr);
            assertEquals(2, hr.getReturn());
            try {
               proxy.helloRequest("number");
               fail();
            } catch (Exception e) {
               assertTrue(e.getMessage().contains("is not facet-valid with respect to enumeration"));
            }
         } finally {
            JBossWSTestHelper.undeploy(DeploymentArchives.SERVER);
         }
      }
      finally
      {
         // -- restore default conf --
         runInContainer("disableDefaultEndpointSchemaValidation");
         // --
      }
      
      
   }
   
   // -------------------------
   
   private String runInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-schemavalidation-client?path=/jaxws-samples-schemavalidation/hello&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }

   
}