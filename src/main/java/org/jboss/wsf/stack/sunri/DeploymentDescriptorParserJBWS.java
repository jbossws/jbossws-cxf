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

// $Id$

import java.net.MalformedURLException;

import javax.ejb.Stateless;

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.transport.http.ResourceLoader;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;

/**
 * A copy of DeploymentDescriptorParser that externalizes
 * 
 * createInvoker(Class)
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DeploymentDescriptorParserJBWS<A> extends DeploymentDescriptorParserExt<A>
{
   public DeploymentDescriptorParserJBWS(ClassLoader cl, ResourceLoader loader, Container container, AdapterFactory<A> adapterFactory) throws MalformedURLException
   {
      super(cl, loader, container, adapterFactory);
   }

   @Override
   protected Invoker createInvoker(Class<?> implClass)
   {
      Invoker invoker;
      InstanceResolver<?> resolver = InstanceResolver.createDefault(implClass);
      if (implClass.isAnnotationPresent(Stateless.class))
      {
         invoker = new InvokerEJB3(resolver);
      }
      else
      {
         invoker = new InvokerJSE(resolver);
      }
      return invoker;
   }

}
