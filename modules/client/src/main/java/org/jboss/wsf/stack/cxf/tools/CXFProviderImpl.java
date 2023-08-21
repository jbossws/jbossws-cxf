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

import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.java2ws.JavaToWS;
import org.jboss.ws.api.tools.WSContractProvider;
import org.jboss.ws.common.utils.NullPrintStream;
import org.jboss.wsf.stack.cxf.i18n.Messages;

/**
 * A WSContractProvider for the CXF stack
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Feb-2009
 */
public class CXFProviderImpl extends WSContractProvider
{
   private ClassLoader loader;
   private boolean generateWsdl;
   private boolean extension;
   private boolean generateSource;
   private File outputDir = new File("output");
   private File resourceDir;
   private File sourceDir;
   private PrintStream messageStream;
   private String portAddress;

   public CXFProviderImpl()
   {
   }

   public void setGenerateWsdl(boolean generateWsdl)
   {
      this.generateWsdl = generateWsdl;
   }

   public void setExtension(boolean extension)
   {
      this.extension = extension;
   }

   public void setGenerateSource(boolean generateSource)
   {
      this.generateSource = generateSource;
   }

   public void setOutputDirectory(File directory)
   {
      this.outputDir = directory;
   }

   public void setResourceDirectory(File directory)
   {
      this.resourceDir = directory;
   }

   public void setSourceDirectory(File directory)
   {
      this.sourceDir = directory;
   }

   public void setClassLoader(ClassLoader loader)
   {
      this.loader = loader;
   }

   public void setMessageStream(PrintStream messageStream)
   {
      this.messageStream = messageStream;
   }
   
   public void setPortSoapAddress(String address)
   {
      this.portAddress = address;
   }

   public void provide(String endpointClass)
   {
      try
      {
         provide(loader.loadClass(endpointClass));
      }
      catch (ClassNotFoundException e)
      {
         throw Messages.MESSAGES.classNotFound(endpointClass);
      }
   }

   public void provide(Class<?> endpointClass)
   {
      // Swap the context classloader
      // The '--classpath' switch might provide an URLClassLoader
      ClassLoader oldLoader = SecurityActions.getContextClassLoader();

      if (loader != null)
         SecurityActions.setContextClassLoader(loader);

      try
      {
         List<String> args = new ArrayList<String>();

         // Use the output directory as the default
         File resourceDir = (this.resourceDir != null) ? this.resourceDir : outputDir;
         File sourceDir = (this.sourceDir != null) ? this.sourceDir : outputDir;

         if (generateSource)
         {
            
         }
         
         if (sourceDir != null)
         {
            if (!sourceDir.exists() && !sourceDir.mkdirs())
               throw MESSAGES.couldNotMakeDirectory(sourceDir.getName());

            args.add("-s");
            args.add(sourceDir.getAbsolutePath());
         }

         if (!outputDir.exists() && !outputDir.mkdirs())
            throw MESSAGES.couldNotMakeDirectory(outputDir.getName());

         args.add("-classdir");
         args.add(outputDir.getAbsolutePath());

         if (resourceDir != null)
         {
            if (!resourceDir.exists() && !resourceDir.mkdirs())
               throw MESSAGES.couldNotMakeDirectory(resourceDir.getName());
            args.add("-d");
            args.add(resourceDir.getAbsolutePath());
         }

         PrintStream stream = messageStream;
         if (stream != null)
         {
            //TODO: There is no need to set verbose to cxf java2ws tool ?
            args.add("-verbose");
         }
         else
         {
            stream = NullPrintStream.getInstance();
         }

         // -wsdl[:protocol]
         if (generateWsdl)
         {
            args.add("-wsdl");
            if (extension)
               args.add("-soap12");
            if (portAddress != null) {
               args.add("-address");
               args.add(portAddress);
            }
         }

         String cp = buildClasspathString(loader);
         if (cp != null)
         {
            args.add("-cp");
            args.add(cp);
         }
         
         args.add("-wrapperbean");
         
         args.add("-createxsdimports");

         // the SEI
         args.add(endpointClass.getCanonicalName());

         JavaToWS j2w = new JavaToWS(args.toArray(new String[0]));
         if (CXFConsumerImpl.getJVMMajorVersion() > 8)
         {
            ToolContext ctx = new ToolContext();
            ctx.put(ToolConstants.CFG_CMD_ARG, args);
            ctx.put(ToolConstants.COMPILER, new Jdk9PlusJBossModulesAwareCompiler());
            j2w.run(ctx, stream);
         } else
         {
            j2w.run(stream);
         }

      }
      catch (Throwable t)
      {
         if (messageStream != null)
         {
            messageStream.println(MESSAGES.failedToInvoke(JavaToWS.class.getName()));
            t.printStackTrace(messageStream);
         }
         else
         {
            t.printStackTrace();
         }
      }
      finally
      {
         SecurityActions.setContextClassLoader(oldLoader);
      }
   }

   private String buildClasspathString(ClassLoader cl)
   {
      String cp = null;

      if (cl instanceof URLClassLoader)
      {
         StringBuilder builder = new StringBuilder();
         URLClassLoader urlLoader = (URLClassLoader)cl;
         for (URL url : urlLoader.getURLs())
         {
            try {
               File f = new File(url.toURI());
               builder.append(f.getAbsolutePath());
            } catch (Exception e) {
               builder.append(url.getPath());
            }
            builder.append(File.pathSeparator);
         }

         if (cl.getParent() != null)
         {
            String parentPath = buildClasspathString(cl.getParent());
            if (parentPath != null)
               builder.append(parentPath);
         }

         cp = builder.toString();
      }

      return cp;
   }
}
