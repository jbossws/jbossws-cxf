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

import org.apache.cxf.common.util.Compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import jakarta.xml.ws.spi.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A CXF Compiler that installs a custom JavaFileManager to load JAXWS and JAXB apis from
 * the proper JBoss module (the one providing the JAXWS SPI Provider) instead of from the
 * JDK boot classpath. This implemenation addresses the needs of JDK-8 and earlier versions.
 */
public final class JBossModulesAwareCompiler extends Compiler
{
   @Override
   protected JavaFileManager wrapJavaFileManager(StandardJavaFileManager standardJavaFileManger)
   {
      return new CustomJavaFileManager(standardJavaFileManger);
   }
}


/**
 * This file manager attempts to retrieve each requested class from the JBoss Module.
 * Each found class is wrapped in a JDK JavaFileObject and registered with the JDK compiler
 * file manager.  Any class not found is to left to the JDK file manager to find in its other
 * constructs.
 */
class CustomJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
   private ClassLoader classLoader = new ClassLoader(Provider.provider().getClass().getClassLoader()) {
      //just prevent the classloader from being Closeable, as URLClassloader implements Closeable since JDK 1.7 u12 b08
   };

   protected CustomJavaFileManager(JavaFileManager fileManager) {
      super(fileManager);
   }

   public ClassLoader getClassLoader(JavaFileManager.Location location) {
      //TODO evaluate replacing with return new DelegateClassLoader(super.getClassLoader(location), classLoader);
      return classLoader;
   }

   @Override
   public FileObject getFileForInput(JavaFileManager.Location location, String packageName, String relativeName) throws IOException {
      return super.getFileForInput(location, packageName, relativeName);
   }

   @Override
   public String inferBinaryName(JavaFileManager.Location loc, JavaFileObject file) {
      String result;
      if (file instanceof JavaFileObjectImpl)
         result = file.getName();
      else
         result = super.inferBinaryName(loc, file);
      return result;
   }

   @Override
   public Iterable<JavaFileObject> list(JavaFileManager.Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse)
           throws IOException {
      Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
      List<JavaFileObject> files = new ArrayList<JavaFileObject>();
      if (location == StandardLocation.PLATFORM_CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS))
      {
         List<JavaFileObject> resultFiltered = new ArrayList<JavaFileObject>();
         for (Iterator<JavaFileObject> it = result.iterator(); it.hasNext(); )
         {
            final JavaFileObject obj = it.next();
            final String objName = obj.getName();
            Class<?> clazz = null;
            final String className = getFullClassName(packageName, objName);
            try
            {
               clazz = classLoader.loadClass(className);
            } catch (Throwable t)
            {
               //NOOP
            }
            boolean added = false;
            if (clazz != null)
            {
               ClassLoader loader = clazz.getClassLoader();
               if (loader != null)
               {
                  files.add(new JavaFileObjectImpl(className, loader));
                  added = true;
               }
            }
            if (!added)
            {
               resultFiltered.add(obj);
            }
         }
         for (JavaFileObject file : resultFiltered)
         {
            files.add(file);
         }
      } else
      {
         for (JavaFileObject file : result)
         {
            files.add(file);
         }
      }
      return files;
   }


   private static String getFullClassName(String packageName, String objName) {
      // * OpenJDK returns objName strings like:
      // "/usr/java/java-1.6.0-openjdk-1.6.0.0.x86_64/lib/ct.sym(META-INF/sym/rt.jar/java/lang/AbstractMethodError.class)"
      // * Oracle & IBM JDK return objName strings like:
      // "AbstractMethodError.class"
      // ... from either of those we need to get
      // "java.lang.AbstractMethodError"
      String cn = objName.substring(0, objName.indexOf(".class"));
      int startIdx = Math.max(cn.lastIndexOf("."), cn.lastIndexOf("/"));
      if (startIdx > 0)
      {
         cn = cn.substring(startIdx + 1);
      }
      // objName.substring(0, objName.length() - 6)
      return packageName + "." + cn;
   }
}
