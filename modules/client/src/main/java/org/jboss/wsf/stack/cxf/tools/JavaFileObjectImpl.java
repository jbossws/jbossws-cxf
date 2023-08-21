/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
