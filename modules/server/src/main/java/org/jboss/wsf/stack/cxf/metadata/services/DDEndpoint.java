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
package org.jboss.wsf.stack.cxf.metadata.services;

import java.io.IOException;
import java.io.Writer;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class DDEndpoint
{
   private String id;
   private String address;
   private String implementor;
   private String invoker;

   public DDEndpoint(String id, String address, String implementor)
   {
      this.id = id;
      this.address = address;
      this.implementor = implementor;
   }

   public void setInvoker(String invoker)
   {
      this.invoker = invoker;
   }
   
   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<jaxws:endpoint id='" + id + "'");
      writer.write(" address='" + address + "'");
      writer.write(" implementor='" + implementor + "'");
      writer.write(">");
      
      // [JBWS-1746] Add support for configurable invoker in cxf.xml
      if (invoker != null)
         writer.write("<jaxws:invoker><bean class='" + invoker + "'/></jaxws:invoker>");
      
      writer.write("</jaxws:endpoint>");
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder("Service");
      str.append("\n id=" + id);
      str.append("\n address=" + address);
      str.append("\n implementor=" + implementor);
      str.append("\n invoker=" + invoker);
      return str.toString();
   }
}