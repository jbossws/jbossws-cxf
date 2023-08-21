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
package org.jboss.test.ws.jaxws.clientConfig;

import java.util.Collections;
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
      ClientConfig defaultConfig = sc.getClientConfig(ClientConfig.STANDARD_CLIENT_CONFIG);
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
   
   public static void changeDefaultClientConfiguration() {
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData("org.jboss.test.ws.jaxws.clientConfig.LogHandler", "Log Handler", null, null, null, null);
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData(null, null, null, Collections.singletonList(handler), false, null);
      List<UnifiedHandlerChainMetaData> postHC = Collections.singletonList(uhcmd);
      
      ClientConfig newDefaultClientConfig = new ClientConfig(ClientConfig.STANDARD_CLIENT_CONFIG, null, postHC, null, null);
      setClientConfigAndReload(newDefaultClientConfig);
   }
   
   public static void setClientConfigAndReload(ClientConfig config) {
      ServerConfig sc = getServerConfig();
      sc.registerClientConfig(config);
      sc.reloadClientConfigs();
   }
   
   public static void addTestCaseClientConfiguration(String testConfigName) {
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData("org.jboss.test.ws.jaxws.clientConfig.RoutingHandler", "Routing Handler", null, null, null, null);
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData(null, null, null, Collections.singletonList(handler), false, null);
      List<UnifiedHandlerChainMetaData> preHC = new LinkedList<UnifiedHandlerChainMetaData>();
      preHC.add(uhcmd);
      setClientConfigAndReload(new ClientConfig(testConfigName, preHC, null, null, null));
   }
   
   public static void removeTestCaseClientConfiguration(String testConfigName) {
      ServerConfig sc = getServerConfig();
      sc.unregisterClientConfig(new ClientConfig(testConfigName, null, null, null, null));
      sc.reloadClientConfigs();
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }
}
