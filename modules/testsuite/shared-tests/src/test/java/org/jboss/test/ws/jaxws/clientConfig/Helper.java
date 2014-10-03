/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.clientConfig;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.Handler;

import org.jboss.test.helper.ClientHelper;
import org.jboss.ws.api.configuration.ClientConfigFeature;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.spi.metadata.config.ClientConfig;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2012
 */
public class Helper implements ClientHelper
{
   private String address;

   public boolean testClientConfigurer()
   {
      ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
      if (configurer == null) {
         return false;
      }
      return "org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer".equals(configurer.getClass().getName());
   }

   public boolean testCustomClientConfigurationFromFile() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      ClientConfigUtil.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      String resStr = port.echo("Kermit");
      return ("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr));
   }

   public boolean testCustomClientConfigurationOnDispatchFromFile() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

      BindingProvider bp = (BindingProvider)dispatch;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      ClientConfigUtil.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
      String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
      return ("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr));
   }

   public boolean testCustomClientConfigurationFromFileUsingFeature() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class, new ClientConfigFeature("META-INF/jaxws-client-config.xml", "Custom Client Config"));

      BindingProvider bp = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      String resStr = port.echo("Kermit");
      return ("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr));
   }

   public boolean testCustomClientConfigurationFromFileUsingFeatureOnDispatch() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD,
            new ClientConfigFeature("META-INF/jaxws-client-config.xml", "Custom Client Config"));

      BindingProvider bp = (BindingProvider)dispatch;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
      String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
      return ("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr));
   }

   public boolean testConfigurationChange() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
      configurer.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      String resStr = port.echo("Kermit");
      if (!"Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr)) {
         return false;
      }

      configurer.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Another Client Config");

      resStr = port.echo("Kermit");
      return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
   }

   public boolean testConfigurationChangeOnDispatch() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

      BindingProvider bp = (BindingProvider)dispatch;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);

      ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
      configurer.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
      String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
      if (!"Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr)) {
         return false;
      }

      configurer.setConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Another Client Config");

      resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
      resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
      return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
   }

   /**
    * This test hacks the current ServerConfig temporarily adding an handler from this testcase deployment
    * into the AS default client configuration, verifies the handler is picked up and finally restores the
    * original default client configuration.
    *
    * @return
    * @throws Exception
    */
   public boolean testDefaultClientConfiguration() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");
      final ClientConfig defaultClientConfig = TestUtils.getAndVerifyDefaultClientConfiguration();

      // -- modify default conf --
      try
      {
         TestUtils.changeDefaultClientConfiguration();
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);

         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         String resStr = port.echo("Kermit");
         return ("Kermit|UserOut|LogOut|endpoint|LogIn|UserIn".equals(resStr));
      }
      finally
      {
         // -- restore default conf --
         TestUtils.setClientConfigAndReload(defaultClientConfig);
         // --
      }
   }

   public boolean testDefaultClientConfigurationOnDispatch() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");
      final ClientConfig defaultClientConfig = TestUtils.getAndVerifyDefaultClientConfiguration();

      // -- modify default conf --
      try
      {
         TestUtils.changeDefaultClientConfiguration();
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

         BindingProvider bp = (BindingProvider)dispatch;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
         String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
         return ("Kermit|UserOut|LogOut|endpoint|LogIn|UserIn".equals(resStr));
      }
      finally
      {
         // -- restore default conf --
         TestUtils.setClientConfigAndReload(defaultClientConfig);
         // --
      }
   }

   /**
    * This test hacks the current ServerConfig temporarily adding a test client configuration, uses that
    * for the test client and finally removes it from the ServerConfig.
    *
    * @return
    * @throws Exception
    */
   public boolean testCustomClientConfiguration() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);

         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         ClientConfigUtil.setConfigHandlers(bp, null, testConfigName);

         String resStr = port.echo("Kermit");
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   public boolean testCustomClientConfigurationOnDispatch() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

         BindingProvider bp = (BindingProvider)dispatch;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         ClientConfigUtil.setConfigHandlers(bp, null, testConfigName);

         Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
         String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   public boolean testCustomClientConfigurationUsingFeature() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class, new ClientConfigFeature(null, testConfigName));

         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         String resStr = port.echo("Kermit");
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   public boolean testCustomClientConfigurationOnDispatchUsingFeature() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD, new ClientConfigFeature(null, testConfigName));

         BindingProvider bp = (BindingProvider)dispatch;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
         String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   /**
    * This test hacks the current ServerConfig temporarily adding a test client configuration
    * (named as the SEI class FQN), let the container use that for the test client and
    * finally removes it from the ServerConfig.
    *
    * @return
    * @throws Exception
    */
   public boolean testSEIClassDefaultClientConfiguration() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "org.jboss.test.ws.jaxws.clientConfig.Endpoint";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);

         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         String resStr = port.echo("Kermit");
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   public boolean testSEIClassDefaultClientConfigurationOnDispatch() throws Exception
   {
      final String reqString = "<ns1:echo xmlns:ns1=\"http://clientConfig.jaxws.ws.test.jboss.org/\"><arg0>Kermit</arg0></ns1:echo>";
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      QName portName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointPort");
      URL wsdlURL = new URL(address + "?wsdl");

      final String testConfigName = "org.jboss.test.ws.jaxws.clientConfig.Endpoint";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --

         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

         BindingProvider bp = (BindingProvider)dispatch;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);

         ClientConfigUtil.setConfigHandlers(bp, null, testConfigName);

         Source resSource = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));
         String resStr = DOMUtils.getTextContent(DOMUtils.sourceToElement(resSource).getElementsByTagName("return").item(0));
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         TestUtils.removeTestCaseClientConfiguration(testConfigName);
         // --
      }
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.address = address;
   }
}
