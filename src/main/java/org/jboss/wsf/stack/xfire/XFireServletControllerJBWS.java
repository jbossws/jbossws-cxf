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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.transport.http.XFireServletController;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointAssociation;

/**
 * An extension to the XFire servlet controller
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class XFireServletControllerJBWS extends XFireServletController
{
   public XFireServletControllerJBWS(XFire xfire, ServletContext servletContext)
   {
      super(xfire, servletContext);
   }
   
   protected String getService(HttpServletRequest request)
   {
      Endpoint ep = EndpointAssociation.getEndpoint();
      String serviceName = ep.getShortName();
      return serviceName;
   }
}
