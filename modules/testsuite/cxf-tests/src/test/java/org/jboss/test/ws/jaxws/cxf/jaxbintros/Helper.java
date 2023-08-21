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
package org.jboss.test.ws.jaxws.cxf.jaxbintros;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.jaxb.intros.BindingCustomizationFactory;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.api.binding.JAXBBindingCustomization;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSConfigurer;
import org.jboss.wsf.test.ClientHelper;

/**
 * Test the JAXBIntroduction features.
 * 
 * jaxb-intros.xml can reside under META-INF or WEB-INF and should be
 * picked up by JAXBIntroduction deployment aspect on server side.
 *
 * @author alessio.soldano@jboss.com
 */
public class Helper implements ClientHelper
{
   private String endpointAddress;
   private URL jaxbIntroUrl;
   
   public Helper()
   {
      
   }
   
   public Helper(String endpointAddress)
   {
      this.setTargetEndpoint(endpointAddress);
   }

   public boolean testEndpoint() throws Exception
   {
      Bus bus = setBindingCustomizationOnClientSide();
      try
      {
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/cxf/jaxbintros", "EndpointBeanService");

         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         Endpoint port = service.getPort(Endpoint.class);
         UserType user = new UserType();
         QName qname = new QName("ns", "local", "prefix");
         user.setQname(qname);
         user.setString("Foo");
         UserType result = port.echo(user);
         if (!"Foo".equals(result.getString())) {
            return false;
         }
         if (!qname.equals(result.getQname())) {
            return false;
         }
      }
      finally
      {
         bus.shutdown(true);
      }
      return true;
   }

   public boolean testAnnotatedUserEndpoint() throws Exception
   {
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/cxf/jaxbintros", "EndpointBeanService");

      Service service = Service.create(wsdlURL, serviceName);
      AnnotatedUserEndpoint port = service.getPort(AnnotatedUserEndpoint.class);
      AnnotatedUserType user = new AnnotatedUserType();
      QName qname = new QName("ns", "local", "prefix");
      user.setQname(qname);
      user.setString("Foo");
      AnnotatedUserType result = port.echo(user);
      if (!"Foo".equals(result.getString())) {
         return false;
      }
      if (!qname.equals(result.getQname())) {
         return false;
      }
      return true;
   }

   /**
    * Setup binding customization on client side using the JBossWSConfigurer
    *
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   private Bus setBindingCustomizationOnClientSide() throws Exception
   {
      BindingCustomization jaxbCustomizations = new JAXBBindingCustomization();
      if (jaxbIntroUrl == null)
      {
         jaxbIntroUrl = Thread.currentThread().getContextClassLoader().getResource("jaxb-intros.xml");
      }
      BindingCustomizationFactory.populateBindingCustomization(jaxbIntroUrl.openStream(), jaxbCustomizations);

      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      JBossWSConfigurer configurer = (JBossWSConfigurer)bus.getExtension(Configurer.class);
      configurer.getCustomizer().setBindingCustomization(jaxbCustomizations);
      return bus;
   }
   
   public void setJAXBIntroURL(URL url)
   {
      this.jaxbIntroUrl = url;
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.endpointAddress = address;
   }
}
