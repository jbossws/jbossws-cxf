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
package org.jboss.test.ws.jaxws.jbws3282;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;

/**
 * Support utils for endpoint config testcase; this basically collects methods
 * for test purposes only to allow having a clean testcase without dependencies
 * the user is not actually going to need.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2014
 */
public class TestUtils
{
   public static EndpointConfig getAndVerifyDefaultEndpointConfiguration() throws Exception {
      ServerConfig sc = getServerConfig();
      EndpointConfig defaultConfig = sc.getEndpointConfig(EndpointConfig.STANDARD_ENDPOINT_CONFIG);
      if (defaultConfig == null) {
         throw new Exception("Missing AS endpoint config '" + EndpointConfig.STANDARD_ENDPOINT_CONFIG + "'!");
      }
      List<UnifiedHandlerChainMetaData> preHC = defaultConfig.getPreHandlerChains();
      List<UnifiedHandlerChainMetaData> postHC = defaultConfig.getPostHandlerChains();
      if ((preHC != null && !preHC.isEmpty()) || (postHC != null && !postHC.isEmpty())) {
         throw new Exception("'" + EndpointConfig.STANDARD_ENDPOINT_CONFIG + "' is not empty!");
      }
      return defaultConfig;
   }
   
   public static void changeDefaultEndpointConfiguration() {
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData("org.jboss.test.ws.jaxws.jbws3282.LogHandler", "Log Handler", null, null, null, null);
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData(null, null, null, Collections.singletonList(handler), false, null);
      List<UnifiedHandlerChainMetaData> postHC = Collections.singletonList(uhcmd);
      
      EndpointConfig newDefaultEndpointConfig = new EndpointConfig(EndpointConfig.STANDARD_ENDPOINT_CONFIG, null, postHC, null, null);
      setEndpointConfigAndReload(newDefaultEndpointConfig);
   }
   
   public static void setEndpointConfigAndReload(EndpointConfig config) {
      ServerConfig sc = getServerConfig();
      sc.registerEndpointConfig(config);
      sc.reloadEndpointConfigs();
   }
   
   public static void addTestCaseEndpointConfiguration(String testConfigName) {
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData("org.jboss.test.ws.jaxws.jbws3282.RoutingHandler", "Routing Handler", null, null, null, null);
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData(null, null, null, Collections.singletonList(handler), false, null);
      List<UnifiedHandlerChainMetaData> preHC = new LinkedList<UnifiedHandlerChainMetaData>();
      preHC.add(uhcmd);
      setEndpointConfigAndReload(new EndpointConfig(testConfigName, preHC, null, null, null));
   }
   
   public static void removeTestCaseEndpointConfiguration(String testConfigName) {
      ServerConfig sc = getServerConfig();
      sc.unregisterEndpointConfig(new EndpointConfig(testConfigName, null, null, null, null));
      sc.reloadEndpointConfigs();
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }
}
