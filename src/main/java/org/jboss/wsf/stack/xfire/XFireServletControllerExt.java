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
package org.jboss.wsf.stack.xfire;

//$Id$

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.transport.http.XFireServletController;
import org.jboss.logging.Logger;
import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointAssociation;

/**
 * An extension to the XFire servlet controller
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class XFireServletControllerExt extends XFireServletController
{
   private static Logger log = Logger.getLogger(XFireServletControllerExt.class);

   public XFireServletControllerExt(XFire xfire, ServletContext servletContext)
   {
      super(xfire, servletContext);
   }

   protected String getService(HttpServletRequest request)
   {
      Endpoint ep = EndpointAssociation.getEndpoint();
      String serviceName = ep.getShortName();
      return serviceName;
   }

   protected void generateWSDL(HttpServletResponse response, String service) throws ServletException, IOException
   {
      Service userService = getXFire().getServiceRegistry().getService(service);
      Object value = userService.getProperty(Service.DISABLE_WSDL_GENERATION);
      boolean isWSDLDisabled = "true".equalsIgnoreCase((value != null ? value.toString() : null));
      if (isWSDLDisabled)
      {
         log.warn("WSDL generation disabled for service :" + service);
         response.sendError(404, "No wsdl is avaiable for this service");
         return;
      }

      response.setStatus(200);
      response.setContentType("text/xml");

      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Endpoint ep = EndpointAssociation.getEndpoint();
         UnifiedVirtualFile rootFile = ep.getService().getDeployment().getRootFile();
         Thread.currentThread().setContextClassLoader(new VirtualFileClassLoader(rootFile, ctxLoader));
         getXFire().generateWSDL(service, response.getOutputStream());
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
   }
   
   /*
    * For some reason WebAppClassLoader.getResource("WEB-INF/wsdl/TestEndpoint.wsdl")
    * fails when the war is deployed as part of an ear deployment.
    * 
    * This ClassLoader falls back to the VFS to find the resource URL. 
    */
   static class VirtualFileClassLoader extends ClassLoader
   {
      private UnifiedVirtualFile vFile;
      
      public VirtualFileClassLoader(UnifiedVirtualFile file, ClassLoader parent)
      {
         super(parent);
         vFile = file;
      }

      @Override
      public URL getResource(String name)
      {
         URL url = super.getResource(name);
         if (url == null)
         {
            try
            {
               url = vFile.findChild(name).toURL();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
         return url;
      }
   }
}
