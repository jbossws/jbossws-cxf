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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;

/**
 * A servlet for returning the current data dir from the server config;
 * this is required for WSDLPublishTestCase.
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Apr-2010
 *
 */
public class SupportServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   
   private File dataDir;
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      res.getWriter().print(getDataDir().getAbsolutePath());
   }
   
   private File getDataDir()
   {
      if (dataDir == null)
      {
         ClassLoader loader = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         ServerConfig serverConfig = spiProvider.getSPI(ServerConfigFactory.class, loader).getServerConfig();
         dataDir = serverConfig.getServerDataDir();
      }
      return dataDir;
   }
}
