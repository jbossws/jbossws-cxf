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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

//$Id$

/**
 * Metadata model for sun-jaxws.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DDHandlerChain
{
   // Name of the endpoint
   private String name;

   private List<DDHandler> handlers = new ArrayList<DDHandler>();

   public DDHandlerChain(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public List<DDHandler> getEndpoints()
   {
      return handlers;
   }

   public void addEndpoint(DDHandler handler)
   {
      handlers.add(handler);
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<handler-chain>");
      writer.write("<handler-chain-name>" + name + "</handler-chain-name>");
      for (DDHandler handler : handlers)
      {
         handler.writeTo(writer);
      }
      writer.write("</handler-chain>");
   }
}