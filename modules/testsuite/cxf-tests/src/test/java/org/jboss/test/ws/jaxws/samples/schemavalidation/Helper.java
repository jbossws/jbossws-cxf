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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

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
      
      // then modify default conf to enable default client schema validation
      try
      {
         modifyDefaultClientConfiguration(getAndVerifyDefaultConfiguration(true));
         
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
         cleanupConfig(true);
         // --
      }
   }
   
   public boolean enableDefaultEndpointSchemaValidation() throws Exception {
      modifyDefaultClientConfiguration(getAndVerifyDefaultConfiguration(false));
      return true;
   }
   
   public boolean disableDefaultEndpointSchemaValidation() throws Exception {
      cleanupConfig(false);
      return true;
   }
   
   @Override
   public void setTargetEndpoint(String address)
   {
      this.address = address;
   }
   
   protected static AbstractCommonConfig getAndVerifyDefaultConfiguration(boolean client) throws Exception {
      ServerConfig sc = getServerConfig();
      AbstractCommonConfig defaultConfig = null;
      final List<? extends AbstractCommonConfig> cfgs = client ? sc.getClientConfigs() : sc.getEndpointConfigs();
      final String DEFCFG = client ? ClientConfig.STANDARD_CLIENT_CONFIG : EndpointConfig.STANDARD_ENDPOINT_CONFIG;
      for (AbstractCommonConfig c : cfgs) {
         if (DEFCFG.equals(c.getConfigName())) {
            defaultConfig = c;
         }
      }
      if (defaultConfig == null) {
         throw new Exception("Missing AS config '" + DEFCFG + "'!");
      }
      Map<String, String> props = defaultConfig.getProperties();
      if (props != null && !props.isEmpty()) {
         throw new Exception("'" + DEFCFG + "' is not empty!");
      }
      return defaultConfig;
   }
   
   protected static void modifyDefaultClientConfiguration(AbstractCommonConfig defaultConfig) {
      defaultConfig.setProperty("schema-validation-enabled", "true");
   }
   
   protected static void cleanupConfig(boolean client) throws Exception {
      ServerConfig sc = getServerConfig();
      AbstractCommonConfig defaultConfig = null;
      final List<? extends AbstractCommonConfig> cfgs = client ? sc.getClientConfigs() : sc.getEndpointConfigs();
      final String DEFCFG = client ? ClientConfig.STANDARD_CLIENT_CONFIG : EndpointConfig.STANDARD_ENDPOINT_CONFIG;
      for (AbstractCommonConfig c : cfgs) {
         if (DEFCFG.equals(c.getConfigName())) {
            defaultConfig = c;
         }
      }
      if (defaultConfig == null) {
         throw new Exception("Missing AS config '" + DEFCFG + "'!");
      }
      Map<String, String> props = defaultConfig.getProperties();
      if (props == null || props.isEmpty()) {
         throw new Exception("'" + DEFCFG + "' is already empty!");
      }
      props.clear();
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }
}
