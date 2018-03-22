/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.tools;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is a wrapper for class information that is registered with the JDK's
 * file manager.
 */
public final class JavaFileObjectImpl extends SimpleJavaFileObject
{
   private ClassLoader loader;
   private final String key;

   JavaFileObjectImpl(String fqClassName, ClassLoader loader)
   {
      super(toURI(fqClassName), JavaFileObject.Kind.CLASS);
      this.loader = loader;
      this.key = "/" + fqClassName.replace(".", "/") + ".class";
   }

   @Override
   public InputStream openInputStream()
   {
      return loader.getResourceAsStream(key);
   }

   @Override
   public OutputStream openOutputStream()
   {
      throw new UnsupportedOperationException();
   }

   private static URI toURI(String name)
   {
      try
      {
         return new URI(name);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
}
