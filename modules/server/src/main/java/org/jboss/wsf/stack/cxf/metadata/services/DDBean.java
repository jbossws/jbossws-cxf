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

//$Id$

import java.io.IOException;
import java.io.Writer;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class DDBean
{
   private String beanName;
   private String beanClass;

   public DDBean(String beanName, String beanClass)
   {
      this.beanName = beanName;
      this.beanClass = beanClass;
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<bean");
      if (beanName != null)
         writer.write(" name='" + beanName + "'");
      writer.write(" class='" + beanClass + "'");
      writer.write("></bean>");
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder("Bean");
      str.append("\n name=" + beanName);
      str.append("\n class=" + beanClass);
      return str.toString();
   }
}