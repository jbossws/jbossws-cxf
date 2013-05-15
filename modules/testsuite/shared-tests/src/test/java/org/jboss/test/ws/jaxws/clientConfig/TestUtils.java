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
package org.jboss.test.ws.jaxws.clientConfig;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;

/**
 * Support utils for client config testcase; this basically collects methods
 * for test purposes only to allow having a clean testcase without dependencies
 * the user is not actually going to need.
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Jun-2012
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
      List<UnifiedHandlerChainMetaData> preHC = defaultConfig.getPreHandlerChains();
      List<UnifiedHandlerChainMetaData> postHC = defaultConfig.getPostHandlerChains();
      if ((preHC != null && !preHC.isEmpty()) || (postHC != null && !postHC.isEmpty())) {
         throw new Exception("'" + ClientConfig.STANDARD_CLIENT_CONFIG + "' is not empty!");
      }
      return defaultConfig;
   }
   
   public static void modifyDefaultClientConfiguration(ClientConfig defaultConfig) {
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData();
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData();
      handler.setHandlerClass("org.jboss.test.ws.jaxws.clientConfig.LogHandler");
      handler.setHandlerName("Log Handler");
      uhcmd.addHandler(handler);
      List<UnifiedHandlerChainMetaData> postHC = new LinkedList<UnifiedHandlerChainMetaData>();
      postHC.add(uhcmd);
      defaultConfig.setPostHandlerChains(postHC);
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
      List<UnifiedHandlerChainMetaData> preHC = defaultConfig.getPreHandlerChains();
      List<UnifiedHandlerChainMetaData> postHC = defaultConfig.getPostHandlerChains();
      if ((preHC == null || preHC.isEmpty()) && (postHC == null || postHC.isEmpty())) {
         throw new Exception("'" + ClientConfig.STANDARD_CLIENT_CONFIG + "' is already empty!");
      }
      defaultConfig.setPostHandlerChains(null);
      defaultConfig.setPreHandlerChains(null);
   }
   
   public static void addTestCaseClientConfiguration(String testConfigName) {
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData();
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData();
      handler.setHandlerClass("org.jboss.test.ws.jaxws.clientConfig.RoutingHandler");
      handler.setHandlerName("Routing Handler");
      uhcmd.addHandler(handler);
      ClientConfig config = new ClientConfig();
      config.setConfigName(testConfigName);
      List<UnifiedHandlerChainMetaData> preHC = new LinkedList<UnifiedHandlerChainMetaData>();
      preHC.add(uhcmd);
      config.setPreHandlerChains(preHC);
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
