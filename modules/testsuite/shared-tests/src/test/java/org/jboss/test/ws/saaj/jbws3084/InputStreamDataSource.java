/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.saaj.jbws3084;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataSource;

public class InputStreamDataSource implements DataSource
{
   private InputStream is;

   private String contentType;

   private String name;

   public InputStreamDataSource(InputStream is, String contentType, String name)
   {
      this.is = is;
      this.contentType = contentType;
      this.name = name;
   }

   @Override
   public String getContentType()
   {
      return contentType;
   }

   @Override
   public InputStream getInputStream() throws IOException
   {
      return is;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public OutputStream getOutputStream() throws IOException
   {
      throw new UnsupportedOperationException();
   }
}
