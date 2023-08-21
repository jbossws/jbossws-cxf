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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.jboss.ws.api.configuration.ClientConfigFeature;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.test.ClientHelper;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 04-Sep-2012
 */
public class Helper implements ClientHelper
{
   private final QName serviceName = new QName("http://clientConfig.cxf.jaxws.ws.test.jboss.org/", "EndpointImplService");
   private final QName portName = new QName("http://clientConfig.cxf.jaxws.ws.test.jboss.org/", "EndpointPort");
   private String address;

   public void testNullConfigurationThrowsExceptionWhenClientConfigProviderFileNotFound() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      try {
         ClientConfigUtil.setConfigProperties(port, null, null);
         fail("Configuration not found ex should be thrown.");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("not found"));
      }
   }

   public boolean testCustomClientConfigurationFromFile() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      
      ClientConfigUtil.setConfigProperties(port, "META-INF/jaxws-client-config.xml", "Custom Client Config");
      
      return ClientProxy.getClient(port).getEndpoint().get("propA").equals("fileValueA");
   }
   
   public boolean testCustomClientConfigurationOnDispatchFromFile() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
      
      ClientConfigUtil.setConfigProperties(dispatch, "META-INF/jaxws-client-config.xml", "Custom Client Config");
      
      return ((DispatchImpl<?>)dispatch).getClient().getEndpoint().get("propA").equals("fileValueA");
   }
   
   public boolean testCustomClientConfigurationFromFileUsingFeature() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class, new ClientConfigFeature("META-INF/jaxws-client-config.xml", "Custom Client Config", true));
      
      return ClientProxy.getClient(port).getEndpoint().get("propA").equals("fileValueA");
   }
   
   public boolean testCustomClientConfigurationOnDispatchFromFileUsingFeature() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE,
            new ClientConfigFeature("META-INF/jaxws-client-config.xml", "Custom Client Config", true));
      
      return ((DispatchImpl<?>)dispatch).getClient().getEndpoint().get("propA").equals("fileValueA");
   }
   
   public boolean testConfigurationChange() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      org.apache.cxf.endpoint.Endpoint ep = ClientProxy.getClient(port).getEndpoint();
      assert(ep.get("propA") == null);
      assert(ep.get("propB") == null);
      ep.put("propZ", "valueZ");
      
      ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
      configurer.setConfigProperties(port, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      if (!ep.get("propA").equals("fileValueA") || !ep.get("propB").equals("fileValueB") || !ep.get("propZ").equals("valueZ")) {
         return false;
      }
      
      port.echo("Kermit");
      
      configurer.setConfigProperties(port, "META-INF/jaxws-client-config.xml", "Another Client Config");
      
      return (ep.get("propA") == null && ep.get("propB") == null && ep.get("propC").equals("fileValueC") && ep.get("propZ").equals("valueZ"));
   }
   
   public boolean testConfigurationChangeOnDispatch() throws Exception
   {
      Service service = Service.create(new URL(address + "?wsdl"), serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
      org.apache.cxf.endpoint.Endpoint ep = ((DispatchImpl<SOAPMessage>)dispatch).getClient().getEndpoint();
      assert(ep.get("propA") == null);
      assert(ep.get("propB") == null);
      ep.put("propZ", "valueZ");
      
      ClientConfigurer configurer = ClientConfigUtil.resolveClientConfigurer();
      configurer.setConfigProperties(dispatch, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      if (!ep.get("propA").equals("fileValueA") || !ep.get("propB").equals("fileValueB") || !ep.get("propZ").equals("valueZ")) {
         return false;
      }
      
      configurer.setConfigProperties(dispatch, "META-INF/jaxws-client-config.xml", "Another Client Config");
      
      return (ep.get("propA") == null && ep.get("propB") == null && ep.get("propC").equals("fileValueC") && ep.get("propZ").equals("valueZ"));
   }
   
   /**
    * This test hacks the current ServerConfig temporarily adding a property into the AS default client configuration,
    * verifies the handler is picked up and finally restores the original default client configuration. 
    * 
    * @return
    * @throws Exception
    */
   public boolean testDefaultClientConfiguration() throws Exception
   {
      final URL wsdlURL = new URL(address + "?wsdl");
      final ClientConfig defaultClientConfig = TestUtils.getAndVerifyDefaultClientConfiguration();
      
      // -- modify default conf --
      try
      {
         
         final Map<String, String> props = new HashMap<String, String>();
         props.put("propA", "valueA");
         TestUtils.registerClientConfigAndReload(new ClientConfig(defaultClientConfig.getConfigName(), null, null, props, null));
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         
         return (ClientProxy.getClient(port).getEndpoint().get("propA").equals("valueA"));
      }
      finally
      {
         // -- restore default conf --
         TestUtils.registerClientConfigAndReload(defaultClientConfig);
         // --
      }
   }
   
   public boolean testDefaultClientConfigurationOnDispatch() throws Exception
   {
      final URL wsdlURL = new URL(address + "?wsdl");
      final ClientConfig defaultClientConfig = TestUtils.getAndVerifyDefaultClientConfiguration();
      
      // -- modify default conf --
      try
      {
         final Map<String, String> props = new HashMap<String, String>();
         props.put("propA", "valueA");
         TestUtils.registerClientConfigAndReload(new ClientConfig(defaultClientConfig.getConfigName(), null, null, props, null));
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
         
         return (((DispatchImpl<SOAPMessage>)dispatch).getClient().getEndpoint().get("propA").equals("valueA"));
      }
      finally
      {
         // -- restore default conf --
         TestUtils.registerClientConfigAndReload(defaultClientConfig);
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
      final URL wsdlURL = new URL(address + "?wsdl");
      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         org.apache.cxf.endpoint.Endpoint ep = ClientProxy.getClient(port).getEndpoint();
         ep.put("propZ", "valueZ");
         
         ClientConfigUtil.setConfigProperties(port, null, testConfigName);
         
         return (ep.get("propT").equals("valueT") && ep.get("propZ").equals("valueZ"));
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
      final URL wsdlURL = new URL(address + "?wsdl");
      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
         org.apache.cxf.endpoint.Endpoint ep = ((DispatchImpl<SOAPMessage>)dispatch).getClient().getEndpoint();
         ep.put("propZ", "valueZ");
         
         ClientConfigUtil.setConfigProperties(dispatch, null, testConfigName);
         
         return (ep.get("propT").equals("valueT") && ep.get("propZ").equals("valueZ"));
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
      final URL wsdlURL = new URL(address + "?wsdl");
      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class, new ClientConfigFeature(null, testConfigName, true));
         org.apache.cxf.endpoint.Endpoint ep = ClientProxy.getClient(port).getEndpoint();
         ep.put("propZ", "valueZ");
         
         return (ep.get("propT").equals("valueT") && ep.get("propZ").equals("valueZ"));
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
      final URL wsdlURL = new URL(address + "?wsdl");
      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         TestUtils.addTestCaseClientConfiguration(testConfigName);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE, new ClientConfigFeature(null, testConfigName, true));
         org.apache.cxf.endpoint.Endpoint ep = ((DispatchImpl<SOAPMessage>)dispatch).getClient().getEndpoint();
         ep.put("propZ", "valueZ");
         
         return (ep.get("propT").equals("valueT") && ep.get("propZ").equals("valueZ"));
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
