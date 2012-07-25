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
package org.jboss.wsf.stack.cxf.tools;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;
import org.jboss.ws.api.tools.WSContractConsumer;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.NullPrintStream;

/**
 * A WSContractConsumer for CXF stack
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Feb-2009
 */
public class CXFConsumerImpl extends WSContractConsumer
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(CXFConsumerImpl.class);
   private List<File> bindingFiles = null;
   private File catalog = null;
   private boolean extension;
   private boolean generateSource = false;
   private File outputDir = new File("output");
   private File sourceDir = null;
   private String targetPackage = null;
   private PrintStream messageStream = null;
   private String wsdlLocation = null;
   private List<String> additionalCompilerClassPath = new ArrayList<String>();
   private boolean additionalHeaders = false;
   private String target;
   private boolean nocompile = false;

   @Override
   public void setBindingFiles(List<File> bindingFiles)
   {
      this.bindingFiles = bindingFiles;
   }

   @Override
   public void setCatalog(File catalog)
   {
      this.catalog = catalog;
   }

   @Override
   public void setExtension(boolean extension)
   {
      this.extension = extension;
   }

   @Override
   public void setGenerateSource(boolean generateSource)
   {
      this.generateSource = generateSource;
   }

   @Override
   public void setMessageStream(PrintStream messageStream)
   {
      this.messageStream = messageStream;
   }

   @Override
   public void setOutputDirectory(File directory)
   {
      outputDir = directory;
   }

   @Override
   public void setSourceDirectory(File directory)
   {
      sourceDir = directory;
   }

   @Override
   public void setTargetPackage(String targetPackage)
   {
      this.targetPackage = targetPackage;
   }

   @Override
   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   @Override
   public void setAdditionalCompilerClassPath(List<String> additionalCompilerClassPath)
   {
      this.additionalCompilerClassPath = additionalCompilerClassPath;
   }
   
   @Override
   public void setAdditionalHeaders(boolean additionalHeaders)
   {
      this.additionalHeaders = additionalHeaders;
   }

   @Override
   public void setTarget(String target)
   {
      this.target = target;
   }

   @Override
   public void setNoCompile(boolean nocompile)
   {
      this.nocompile = nocompile;
   }

   @Override
   public void consume(URL wsdl)
   {
      List<String> args = new ArrayList<String>();
      
      PrintStream stream = messageStream;
      boolean verbose = false;
      if (stream != null)
      {
         verbose = true;
      }
      else
      {
         stream = NullPrintStream.getInstance();
      }
      
      // Always set the target
      if ("2.1".equals(target))
      {
         args.add("-frontend");
         args.add("jaxws21");
      }
      else if (target != null && !target.equals("2.2"))
      {
         stream.println("Unsupported target, using default value '2.2'");
      }
      
      if (bindingFiles != null)
      {
         for (File file : bindingFiles)
         {
            args.add("-b");
            args.add(file.getAbsolutePath());

         }
      }

      if (catalog != null)
      {
         args.add("-catalog");
         args.add(catalog.getAbsolutePath());
      }

      if (!nocompile)
      {
         args.add("-compile");
      }
      
      args.add("-exsh");
      args.add(additionalHeaders ? "true" : "false");
      
      if (targetPackage != null)
      {
         args.add("-p");
         args.add(targetPackage);
      }
      
      File sourceTempDir = null;
      if (generateSource) {
         if (sourceDir == null)
         {
            sourceDir = outputDir;
         }
         if (!sourceDir.exists() && !sourceDir.mkdirs())
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "COULD_NOT_MAKE_DIRECTORY",  sourceDir.getName()));

         args.add("-d");
         args.add(sourceDir.getAbsolutePath());
      } else {
         sourceTempDir = new File(outputDir, "tmp" + Math.round(Math.random() * 10000000));
         FileUtils.mkDir(sourceTempDir);
         args.add("-d");
         args.add(sourceTempDir.getAbsolutePath());
      }

      if (wsdlLocation != null)
      {
         args.add("-wsdlLocation");
         args.add(wsdlLocation);
      }

      if (verbose) {
         args.add("-verbose");
      }

      if (extension)
      {
         stream.println("TODO! Cheek SOAP 1.2 extension");
      }

      if (!outputDir.exists() && !outputDir.mkdirs())
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "COULD_NOT_MAKE_DIRECTORY",  outputDir.getName()));

      // Always add the output directory and the wsdl location
      if (!nocompile)
      {
         args.add("-classdir");
         args.add(outputDir.getAbsolutePath());
      }
     
      //Always generate wrapped style for reference element:CXF-1079
      args.add("-allowElementReferences");
      
      // finally the WSDL file
      args.add(wsdl.toString());

      // See WsimportTool#compileGeneratedClasses()
      if (!additionalCompilerClassPath.isEmpty())
      {
         StringBuffer javaCP = new StringBuffer();
         for (String s : additionalCompilerClassPath)
         {
            javaCP.append(s).append(File.pathSeparator);
         }
         System.setProperty("java.class.path", javaCP.toString());
      }
      
      
      WSDLToJava w2j = new WSDLToJava(args.toArray(new String[0]));
      try
      {
         w2j.run(new ToolContext(), stream);
      }
      catch (Throwable t)
      {
         if (messageStream != null)
         {
            messageStream.println("Failed to invoke WSDLToJava");
            t.printStackTrace(messageStream);
         }
         else
         {
            t.printStackTrace();
         }
      }
      finally
      {
         if (sourceTempDir != null)
         {
            FileUtils.removeDir(sourceTempDir);
         }
      }
   }
}
