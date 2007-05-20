/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.wsf.stack.xfire.metadata.sunjaxws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.wsf.spi.deployment.WSDeploymentException;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;

//$Id$

/**
 * Metadata model for sun-jaxws.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DDEndpoints
{
   private List<DDEndpoint> endpoints = new ArrayList<DDEndpoint>();
   private File tmpFile;

   public List<DDEndpoint> getEndpoints()
   {
      return endpoints;
   }

   public void addEndpoint(DDEndpoint ep)
   {
      endpoints.add(ep);
   }

   public URL createFileURL() 
   {
      destroyFileURL();
      
      ServerConfig serverConfig = ServerConfigFactory.getInstance().getServerConfig();
      File tmpDir = serverConfig.getServerTempDir();
      try
      {
         tmpFile = File.createTempFile("jbossws-sun-jaxws", ".xml", tmpDir);
         Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile));
         writeTo(writer);
         writer.close();
         
         return tmpFile.toURL();
      }
      catch (IOException ex)
      {
         throw new WSDeploymentException(ex);
      }
   }

   public void destroyFileURL()
   {
      if (tmpFile != null)
      {
         tmpFile.delete();
         tmpFile = null;
      }
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<endpoints xmlns='http://java.sun.com/xml/ns/jax-ws/ri/runtime' version='2.0'>");
      for (DDEndpoint ep : endpoints)
      {
         ep.writeTo(writer);
      }
      writer.write("</endpoints>");
   }

}