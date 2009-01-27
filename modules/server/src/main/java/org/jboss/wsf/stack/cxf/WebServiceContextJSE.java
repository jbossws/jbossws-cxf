/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.wsf.spi.invocation.WebServiceContextDelegate;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-Jan-2009
 */
public class WebServiceContextJSE extends WebServiceContextDelegate
{
   private HttpServletRequest httpRequest;
   
   public WebServiceContextJSE(WebServiceContext ctx)
   {
      super(ctx);
      httpRequest = (HttpServletRequest)ctx.getMessageContext().get(MessageContext.SERVLET_REQUEST);
      if (httpRequest == null)
       throw new IllegalStateException("Cannot obtain HTTPServletRequest from message context");
   }

   @Override
   public Principal getUserPrincipal()
   {
      Principal principal = httpRequest.getUserPrincipal();
      return principal;
   }

   @Override
   public boolean isUserInRole(String role)
   {
      boolean isUserInRole = httpRequest.isUserInRole(role);
      return isUserInRole;
   }
}
