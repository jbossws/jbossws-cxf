/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.jboss.wsf.spi.deployment.ServletDelegate;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;

/**
 * An extension to the CXF servlet
 *
 * @author alessio.soldano@jboss.com
 *
 * @since 19-Jan-2016
 */
public class JAXRSServletExt extends CXFNonSpringServlet implements ServletDelegate
{
   private static final long serialVersionUID = 670827645417313373L;

   @Override
   protected void loadBus(ServletConfig servletConfig)
   {
      this.bus = JBossWSBusFactory.getClassLoaderDefaultBus(Thread.currentThread().getContextClassLoader()); //if this is not the dep classloader, pass it through ServletDelegate
      this.bus.setExtension(Thread.currentThread().getContextClassLoader(), ClassLoader.class);
   }

   @Override
   public void doHead(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doHead(request, response);
   }

   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doGet(request, response);
   }

   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPost(request, response);
   }

   @Override
   public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPut(request, response);
   }

   @Override
   public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doDelete(request, response);
   }

   @Override
   public void service(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.service(request, response);
   }
}
