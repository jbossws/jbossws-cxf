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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.test.ws.jaxws.samples.schemavalidation.types.HelloResponse;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.AbstractCommonConfig;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;
import org.jboss.wsf.test.ClientHelper;

/**
 *
 * @author alessio.soldano@jboss.com
 */
public class Helper implements ClientHelper
{
   private String address;
   
   private static final EndpointConfig defaultEndpointConfig = getServerConfig().getEndpointConfig(EndpointConfig.STANDARD_ENDPOINT_CONFIG);
   
   public boolean testDefaultClientValidation() throws Exception
   {
      // first verify schema validation is not enabled yet: a wsdl with schema restrictions is used on client side,
      // but schema validation is not enabled; the invoked endpoint does not have schema validation on
      final QName serviceName = new QName("http://jboss.org/schemavalidation", "HelloService");
      final QName portName = new QName("http://jboss.org/schemavalidation", "HelloPort");
      final URL wsdlURL = Thread.currentThread().getContextClassLoader().getResource("validatingClient.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = (Hello) service.getPort(portName, Hello.class);
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
      HelloResponse hr = proxy.helloRequest("JBoss");
      if (hr == null || hr.getReturn() != 2)
      {
         return false;
      }
      hr = proxy.helloRequest("number");
      if (hr == null || hr.getReturn() != 2)
      {
         return false;
      }
      
      final ClientConfig defClientConfig = (ClientConfig)getAndVerifyDefaultConfiguration(true);
      // then modify default conf to enable default client schema validation
      try
      {
         modifyDefaultConfiguration(true);
         
         service = Service.create(wsdlURL, serviceName);
         proxy = (Hello) service.getPort(portName, Hello.class);
         ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
         hr = proxy.helloRequest("JBoss");
         if (hr == null || hr.getReturn() != 2)
         {
            return false;
         }
         try
         {
            proxy.helloRequest("number");
            return false;
         }
         catch (Exception e)
         {
            return e.getMessage().contains("is not facet-valid with respect to enumeration");
         }
      }
      finally
      {
         // -- restore default conf --
         registerClientConfigAndReload(defClientConfig);
         // --
      }
   }
   
   public boolean enableDefaultEndpointSchemaValidation() throws Exception {
      getAndVerifyDefaultConfiguration(false);
      modifyDefaultConfiguration(false);
      return true;
   }
   
   public boolean disableDefaultEndpointSchemaValidation() throws Exception {
      registerEndpointConfigAndReload(defaultEndpointConfig);
      return true;
   }
   
   @Override
   public void setTargetEndpoint(String address)
   {
      this.address = address;
   }
   
   protected AbstractCommonConfig getAndVerifyDefaultConfiguration(boolean client) throws Exception {
      final String DEFCFG = client ? ClientConfig.STANDARD_CLIENT_CONFIG : EndpointConfig.STANDARD_ENDPOINT_CONFIG;
      final AbstractCommonConfig defaultConfig = client ? getServerConfig().getClientConfig(DEFCFG) : defaultEndpointConfig;
      if (defaultConfig == null) {
         throw new Exception("Missing AS config '" + DEFCFG + "'!");
      }
      Map<String, String> props = defaultConfig.getProperties();
      if (props != null && !props.isEmpty()) {
         throw new Exception("'" + DEFCFG + "' is not empty!");
      }
      return defaultConfig;
   }
   
   protected static void modifyDefaultConfiguration(final boolean client) {
      final Map<String, String> props = new HashMap<String, String>();
      props.put("schema-validation-enabled", "true");
      if (client) {
         registerClientConfigAndReload(new ClientConfig(ClientConfig.STANDARD_CLIENT_CONFIG, null, null, props, null));
      } else {
         registerEndpointConfigAndReload(new EndpointConfig(EndpointConfig.STANDARD_ENDPOINT_CONFIG, null, null, props, null));
      }
   }
   
   protected static void registerClientConfigAndReload(ClientConfig config) {
      ServerConfig sc = getServerConfig();
      sc.registerClientConfig(config);
      sc.reloadClientConfigs();
   }
   
   protected static void registerEndpointConfigAndReload(EndpointConfig config) {
      ServerConfig sc = getServerConfig();
      sc.registerEndpointConfig(config);
      sc.reloadEndpointConfigs();
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }
}
