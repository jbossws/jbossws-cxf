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
package org.jboss.wsf.stack.xfire.metadata.services;

//$Id$

import java.io.IOException;
import java.io.Writer;

/**
 * Metadata model for services.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class DDService
{
   // This will be the name of the service as exposed to the world. Required. 
   private String name;
   // The class name of the object you wish to make into a service. Required. 
   private String serviceClass;
   // The class name of the implementation which you wish to use when the service is invoked. Optional. 
   private String implementationClass;
   // The ServiceFactory controls how the Service is built and configured. Optional. 
   private String serviceFactory;
   // The invoker element is optional. It can be used to set a non-default Invoker for a service. 
   private String invoker;

   public DDService(String name, String serviceClass)
   {
      this.name = name;
      this.serviceClass = serviceClass;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getServiceClass()
   {
      return serviceClass;
   }

   public void setServiceClass(String serviceClass)
   {
      this.serviceClass = serviceClass;
   }

   public String getImplementationClass()
   {
      return implementationClass;
   }

   public void setImplementationClass(String implementationClass)
   {
      this.implementationClass = implementationClass;
   }

   public String getServiceFactory()
   {
      return serviceFactory;
   }

   public void setServiceFactory(String serviceFactory)
   {
      this.serviceFactory = serviceFactory;
   }

   public String getInvoker()
   {
      return invoker;
   }

   public void setInvoker(String invoker)
   {
      this.invoker = invoker;
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<service>");
      writer.write("<name>" + name + "</name>");
      writer.write("<serviceClass>" + serviceClass + "</serviceClass>");
      if (implementationClass != null)
         writer.write("<implementationClass>" + implementationClass + "</implementationClass>");
      if (serviceFactory != null)
         writer.write("<serviceFactory>" + serviceFactory + "</serviceFactory>");
      if (invoker != null)
         writer.write("<invoker>" + invoker + "</invoker>");
      writer.write("</service>");
   }
   
   public String toString()
   {
      StringBuilder str = new StringBuilder("Service");
      str.append("\n name=" + name);
      str.append("\n serviceClass=" + serviceClass);
      if (implementationClass != null)
         str.append("\n implementationClass=" + implementationClass);
      if (serviceFactory != null)
         str.append("\n serviceFactory=" + serviceFactory);
      if (invoker != null)
         str.append("\n invoker=" + invoker);
      return str.toString();
   }
}