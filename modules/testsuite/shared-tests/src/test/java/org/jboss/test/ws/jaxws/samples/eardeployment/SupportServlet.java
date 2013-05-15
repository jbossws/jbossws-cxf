/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
