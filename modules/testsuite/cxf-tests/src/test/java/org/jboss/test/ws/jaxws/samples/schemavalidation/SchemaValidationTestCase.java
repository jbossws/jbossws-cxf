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
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * @author ema@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class SchemaValidationTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-schemavalidation/hello";
   private final String validatingServiceURL = "http://" + getServerHost() + ":8080/jaxws-samples-schemavalidation/validatingHello";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(SchemaValidationTestCase.class, "jaxws-samples-schemavalidation.war");
   }

   public void testSchemaValidationEndpoint() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "ValidatingHelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, validatingServiceURL);
      HelloResponse hr = proxy.helloRequest("JBoss"); //valid value (see xsd restriction in the wsdl)
      assertNotNull(hr);
      assertEquals(1, hr.getReturn());
      try {
         proxy.helloRequest("number");
         fail("validation error is expeced");
      } catch (Exception e) {
         assertTrue("not respect to enumration error is expected", e.getMessage().contains("is not facet-valid with respect to enumeration"));
      }
   }
   
   public void testNoSchemaValidationEndpoint() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/samples/schemavalidation/WEB-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "HelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      HelloResponse hr = proxy.helloRequest("JBoss");
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
      hr = proxy.helloRequest("number"); //validation is not enabled...
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
   }
   
   public void testClientSideSchemaValidation() throws Exception
   {
      QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      URL wsdlURL = getResourceURL("jaxws/samples/schemavalidation/client.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello)service.getPort(new QName("http://jboss.org/schemavalidation", "HelloPort"), Hello.class);
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      ((BindingProvider)proxy).getRequestContext().put("schema-validation-enabled", true); //enable client side schema validation
      HelloResponse hr = proxy.helloRequest("JBoss");
      assertNotNull(hr);
      assertEquals(2, hr.getReturn());
      try {
         proxy.helloRequest("number");
         fail("validation error is expeced");
      } catch (Exception e) {
         assertTrue("not respect to enumration error is expected", e.getMessage().contains("is not facet-valid with respect to enumeration"));
      }
   }
}