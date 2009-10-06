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
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.jaxb.intros.IntroductionsAnnotationReader;
import org.jboss.jaxb.intros.IntroductionsConfigParser;
import org.jboss.jaxb.intros.configmodel.JaxbIntros;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.stack.cxf.client.configuration.JAXBBindingCustomization;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSCXFConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

import com.sun.xml.bind.api.JAXBRIContext;

/**
 * Test the JAXBIntroduction features.
 * 
 * jaxb-intros.xml can reside under META-INF or WEB-INF and should be
 * picked up by JAXBIntroduction deployment aspect on server side.
 *
 * @author alessio.soldano@jboss.com
 */
public class JAXBIntroTestCase extends JBossWSTest
{
   private String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jaxbintros/EndpointBean";
   private Bus bus;
   private Configurer originalConfigurer;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JAXBIntroTestCase.class, "jaxws-cxf-jaxbintros.jar");
   }
                                     
   @SuppressWarnings("unchecked")
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
      Iterator<Element> it = DOMUtils.getChildElements(wsdl, new QName("http://www.w3.org/2001/XMLSchema","attribute"), true);
      boolean attributeFound = false;
      while (it.hasNext())
      {
         Element el = it.next();
         if ("string".equals(el.getAttribute("name")))
         {
            attributeFound = true;
         }
      }
      assertTrue("<xs:attribute name=\"string\" ..> not found in wsdl", attributeFound);
   }
   
   /**
    * Both client and server side use plain UserType class but have jaxbintros in place to deal with customizations
    * 
    * @throws Exception
    */
   public void testEndpoint() throws Exception
   {
      try
      {
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/cxf/jaxbintros", "EndpointBeanService");
   
         setBindingCustomizationOnClientSide();
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = service.getPort(Endpoint.class);
         UserType user = new UserType();
         QName qname = new QName("ns", "local", "prefix");
         user.setQname(qname);
         user.setString("Foo");
         UserType result = port.echo(user);
         assertEquals("Foo", result.getString());
         assertEquals(qname, result.getQname());
      }
      finally
      {
         restoreConfigurer();
      }
   }
   
   /**
    * Client side uses the annotated user type class, server side uses the plain one but has jaxintros in place
    * 
    * @throws Exception
    */
   public void testAnnotatedUserEndpoint() throws Exception
   {
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/cxf/jaxbintros", "EndpointBeanService");

      setBindingCustomizationOnClientSide();
      Service service = Service.create(wsdlURL, serviceName);
      AnnotatedUserEndpoint port = service.getPort(AnnotatedUserEndpoint.class);
      AnnotatedUserType user = new AnnotatedUserType();
      QName qname = new QName("ns", "local", "prefix");
      user.setQname(qname);
      user.setString("Foo");
      AnnotatedUserType result = port.echo(user);
      assertEquals("Foo", result.getString());
      assertEquals(qname, result.getQname());
   }
   
   /**
    * Setup binding customization on client side using the JBossWSCXFConfigurer
    * 
    * @throws Exception
    */
   private void setBindingCustomizationOnClientSide() throws Exception
   {
      JaxbIntros jaxbIntros = IntroductionsConfigParser.parseConfig(getResourceURL("jaxws/cxf/jaxbintros/META-INF/jaxb-intros.xml").openStream());
      IntroductionsAnnotationReader annotationReader = new IntroductionsAnnotationReader(jaxbIntros);
      String defaultNamespace = jaxbIntros.getDefaultNamespace();
      BindingCustomization jaxbCustomizations = new JAXBBindingCustomization();

      jaxbCustomizations.put(JAXBRIContext.ANNOTATION_READER, annotationReader);
      if(defaultNamespace != null) {
         jaxbCustomizations.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP, defaultNamespace);
      }
      
      bus = BusFactory.getThreadDefaultBus();
      originalConfigurer = bus.getExtension(Configurer.class);
      JBossWSCXFConfigurer configurer = new JBossWSCXFConfigurer(originalConfigurer);
      configurer.setBindingCustomization(jaxbCustomizations);
      bus.setExtension(configurer, Configurer.class);

   }
   
   private void restoreConfigurer()
   {
      bus.setExtension(originalConfigurer, Configurer.class);
   }
}
