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
package org.jboss.wsf.stack.cxf.metadata.services;

import java.io.IOException;
import java.io.Writer;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 */
public class DDEndpoint
{
   private String id;
   private String address;
   private String implementor;
   private String invoker;
   private boolean mtomEnabled;

   public DDEndpoint(String id, String address, String implementor, boolean mtomEnabled)
   {
      this.id = id;
      this.address = address;
      this.implementor = implementor;
      this.mtomEnabled = mtomEnabled;
   }

   public void setInvoker(String invoker)
   {
      this.invoker = invoker;
   }
   
   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<jaxws:endpoint id='" + this.id + "'");
      writer.write(" address='" + this.address + "'");
      writer.write(" implementor='" + this.implementor + "'");
      writer.write(">");
      
      if (this.mtomEnabled)
      {
         writer.write("<jaxws:binding>");
         writer.write("<soap:soapBinding mtomEnabled='" + this.mtomEnabled + "'/>");
         writer.write("</jaxws:binding>");
      }

      if (this.invoker != null)
      {
         writer.write("<jaxws:invoker><bean class='" + this.invoker + "'/></jaxws:invoker>");
      }
      
      writer.write("</jaxws:endpoint>");
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder("Service");
      str.append("\n id=" + this.id);
      str.append("\n address=" + this.address);
      str.append("\n implementor=" + this.implementor);
      str.append("\n invoker=" + this.invoker);
      str.append("\n mtomEnabled=" + this.mtomEnabled);
      return str.toString();
   }
}
