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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

import java.util.Iterator;
import java.util.Map;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;

/**
 * Support utils for client config testcase
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Sep-2012
 */
public class TestUtils
{
   public static ClientConfig getAndVerifyDefaultClientConfiguration() throws Exception {
      ServerConfig sc = getServerConfig();
      ClientConfig defaultConfig = null;
      for (ClientConfig c : sc.getClientConfigs()) {
         if (ClientConfig.STANDARD_CLIENT_CONFIG.equals(c.getConfigName())) {
            defaultConfig = c;
         }
      }
      if (defaultConfig == null) {
         throw new Exception("Missing AS client config '" + ClientConfig.STANDARD_CLIENT_CONFIG + "'!");
      }
      Map<String, String> props = defaultConfig.getProperties();
      if (props != null && !props.isEmpty()) {
         throw new Exception("'" + ClientConfig.STANDARD_CLIENT_CONFIG + "' property set is not empty!");
      }
      return defaultConfig;
   }
   
   public static void cleanupClientConfig() throws Exception {
      ServerConfig sc = getServerConfig();
      ClientConfig defaultConfig = null;
      for (ClientConfig c : sc.getClientConfigs()) {
         if (ClientConfig.STANDARD_CLIENT_CONFIG.equals(c.getConfigName())) {
            defaultConfig = c;
         }
      }
      if (defaultConfig == null) {
         throw new Exception("Missing AS client config '" + ClientConfig.STANDARD_CLIENT_CONFIG + "'!");
      }
      Map<String, String> props = defaultConfig.getProperties();
      if (props == null || props.isEmpty()) {
         throw new Exception("'" + ClientConfig.STANDARD_CLIENT_CONFIG + "' property set is already empty!");
      }
      props.clear();
   }
   
   public static void addTestCaseClientConfiguration(String testConfigName) {
      ClientConfig config = new ClientConfig();
      config.setConfigName(testConfigName);
      config.setProperty("propT", "valueT");
      getServerConfig().addClientConfig(config);
   }
   
   public static void removeTestCaseClientConfiguration(String testConfigName) {
      Iterator<ClientConfig> it = getServerConfig().getClientConfigs().iterator();
      while (it.hasNext()) {
         ClientConfig c = it.next();
         if (testConfigName.equals(c.getConfigName())) {
            it.remove();
            break;
         }
      }
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }
}
