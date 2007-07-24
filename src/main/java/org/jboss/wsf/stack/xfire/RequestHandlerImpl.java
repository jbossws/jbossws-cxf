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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.transport.servlet.ServletController;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;

/**
 * A request handler
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class RequestHandlerImpl implements RequestHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(RequestHandlerImpl.class);

   RequestHandlerImpl()
   {
   }

   public void handleHttpRequest(Endpoint ep, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws ServletException, IOException
   {
      ServletController controller = ep.getAttachment(ServletController.class);
      if (controller == null)
         throw new IllegalStateException("Cannot obtain servlet controller");

      controller.invoke(req, res);
   }

   public void handleRequest(Endpoint endpoint, InputStream inStream, OutputStream outStream, InvocationContext context)
   {
      throw new NotImplementedException();
   }

   public void handleWSDLRequest(Endpoint endpoint, OutputStream outStream, InvocationContext context)
   {
      throw new NotImplementedException();
   }
}
