/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jaxbintros;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.jaxb.intros.BindingCustomizationFactory;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.api.binding.JAXBBindingCustomization;
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

         Service service = Service.create(wsdlURL, serviceName);
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
