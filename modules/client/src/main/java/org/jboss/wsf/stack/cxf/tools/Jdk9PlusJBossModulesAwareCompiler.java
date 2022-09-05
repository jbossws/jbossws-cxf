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
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.Resource;
import org.jboss.wsf.stack.cxf.i18n.Loggers;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A CXF Compiler that installs a custom JavaFileManager to load apis from
 * the JBoss Module instead of from the JDK boot classpath.  This implementation
 * addresses the needs of JDK-9 and later versions.
 */
public final class Jdk9PlusJBossModulesAwareCompiler extends Compiler
{
   private static final String UFS = "/"; // unix file separator

   @Override
   public boolean compileFiles(String[] files)
   {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null) {
         throw new IllegalStateException(
                 "No compiler detected, make sure you are running on top of a JDK instead of a JRE.");
      }
      StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
      Iterable<? extends JavaFileObject> fileList = fileManager.getJavaFileObjectsFromStrings(Arrays
              .asList(files));

      return internalCompile(compiler, wrapJavaFileManager(fileManager), setupDiagnosticListener(),
              fileList);
   }

   protected boolean internalCompile(JavaCompiler compiler,
                                     JavaFileManager fileManager,
                                     DiagnosticListener<JavaFileObject> listener,
                                     Iterable<? extends JavaFileObject> fileList) {
      List<String> args = new ArrayList<>();
      addArgs(args);
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
              listener, args, null, fileList);
      Boolean ret = task.call();
      try {
         fileManager.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return ret;
   }

   @Override
   protected JavaFileManager wrapJavaFileManager(StandardJavaFileManager standardJavaFileManger)
   {
      return new Jdk9PlusCustomJavaFileManager(standardJavaFileManger);
   }



   /**
    * This file manager selects classes from the JBoss Module that start with a
    * requested packagename. Each found class file is wrapped in a JDK JavaFileObject
    * and registered with the JDK compiler file manager.
    */
   final class Jdk9PlusCustomJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
   {
      private ClassLoader moduleclassLoader = Thread.currentThread().getContextClassLoader();

      protected Jdk9PlusCustomJavaFileManager(JavaFileManager fileManager) {
         super(fileManager);
      }

      @Override
      public ClassLoader getClassLoader(JavaFileManager.Location location)
      {
         return moduleclassLoader;
      }

      @Override
      public FileObject getFileForInput(JavaFileManager.Location location, String packageName, String relativeName) throws IOException
      {
         return super.getFileForInput(location, packageName, relativeName);
      }

      @Override
      public String inferBinaryName(JavaFileManager.Location loc, JavaFileObject file)
      {
         String result;
         if (file instanceof JavaFileObjectImpl)
            result = file.getName();
         else
            result = super.inferBinaryName(loc, file);
         return result;
      }

      @Override
      public Iterable<JavaFileObject> list(JavaFileManager.Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse)
              throws IOException
      {
         Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
         List<JavaFileObject> files = new ArrayList<JavaFileObject>();

         if (location == StandardLocation.CLASS_PATH && !result.iterator().hasNext()) {
            Module module = Module.forClassLoader(moduleclassLoader, true);
            if (module != null)
            {
               try
               {
                  // get all files that start with package name
                  String pathName = packageName.replace(".", UFS);
                  Iterator<Resource> resIt = module.iterateResources(
                          new JBossModulePathFilter(pathName));

                  while (resIt.hasNext())
                  {
                     Resource r = resIt.next();
                     String n = r.getName();
                     int indx = n.lastIndexOf(".class");
                     if (indx > 0)
                     {
                        String n1 = n.replace(UFS, ".");
                        String clazzName = n1.substring(0, indx);
                        files.add(new JavaFileObjectImpl(clazzName, moduleclassLoader));
                     }
                  }
               }
               catch (ModuleLoadException mle)
               {
                  Loggers.ROOT_LOGGER.debug("", mle);
               }
               catch (Exception e)
               {
                  Loggers.ROOT_LOGGER.warn("", e);
               }
            }
         }
         else
         {
            for (JavaFileObject file : result)
            {
               files.add(file);
            }
         }
         return files;
      }
   }



   /**
    * A JBoss Module filter that selects all class files in the module with the
    * starting path string.
    */
   class JBossModulePathFilter implements org.jboss.modules.filter.PathFilter {

      private final String path;

      public JBossModulePathFilter (final String path) {
         this.path = path;
      }

      public boolean accept(final String p) {
         if (p.startsWith(path))
         {
            return true;
         }
         return false;
      }

      public boolean equals(Object obj) {
         return obj instanceof JBossModulePathFilter && equals((JBossModulePathFilter) obj);
      }

      public boolean equals(final JBossModulePathFilter obj) {
         return obj != null && obj.path.equals(path);
      }

      public int hashcode() {
         return path.hashCode();
      }
   }
}
