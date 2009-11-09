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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.tools.java2ws.JavaToWS;
import org.jboss.ws.tools.io.NullPrintStream;
import org.jboss.wsf.spi.tools.WSContractProvider;
import org.w3c.dom.Element;

/**
 * A WSContractProvider for the CXF stack
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Feb-2009
 */
public class CXFProviderImpl extends WSContractProvider
{
   private ClassLoader loader;
   private boolean generateWsdl = false;
   private boolean generateSource = false;
   private File outputDir = new File("output");
   private File resourceDir = null;
   private File sourceDir = null;
   private PrintStream messageStream;

   public CXFProviderImpl()
   {
   }

   public void setGenerateWsdl(boolean generateWsdl)
   {
      this.generateWsdl = generateWsdl;
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

   public void provide(String endpointClass)
   {
      try
      {
         initLog4j();
         provide(loader.loadClass(endpointClass));
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException("Class not found: " + endpointClass);
      }
   }

   public void provide(Class<?> endpointClass)
   {
      // Swap the context classloader
      // The '--classpath' switch might provide an URLClassLoader
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();

      if (loader != null)
         Thread.currentThread().setContextClassLoader(loader);

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
               throw new IllegalStateException("Could not make directory: " + sourceDir.getName());

            args.add("-s");
            args.add(sourceDir.getAbsolutePath());
         }

         if (!outputDir.exists() && !outputDir.mkdirs())
            throw new IllegalStateException("Could not make directory: " + outputDir.getName());

         args.add("-classdir");
         args.add(outputDir.getAbsolutePath());

         if (resourceDir != null)
         {
            if (!resourceDir.exists() && !resourceDir.mkdirs())
               throw new IllegalStateException("Could not make directory: " + resourceDir.getName());
            args.add("-d");
            args.add(resourceDir.getAbsolutePath());
         }

         PrintStream stream = messageStream;
         if (stream != null)
         {
            // There is no need to set verbose to cxf java2ws tool
            //args.add("-verbose");
         }
         else
         {
            stream = NullPrintStream.getInstance();
         }

         if (generateWsdl)
         {
            args.add("-wsdl");
         }

         String cp = buildClasspathString(loader);
         if (cp != null)
         {
            args.add("-cp");
            args.add(cp);
         }
         
         args.add("-wrapperbean");

         // the SEI
         args.add(endpointClass.getCanonicalName());
         
         JavaToWS j2w = new JavaToWS(args.toArray(new String[0]));
         j2w.run(stream);
      }
      catch (Throwable t)
      {
         if (messageStream != null)
         {
            messageStream.println("Failed to invoke JavaToWS");
            t.printStackTrace(messageStream);
         }
         else
         {
            t.printStackTrace();
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
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
            builder.append(url.getPath());
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

   /**
    * cxf java2ws tool requires log4j configuration;this method will configure log4j when running in command line and avoid the log4j not configure error.
    */
   private void initLog4j() {
      //TODO: look at if it is possible to set jboss LoggerRepository 
      String xmlConfig = "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\" debug=\"false\">"
         +   "<appender name=\"CONSOLE\" class=\"org.apache.log4j.ConsoleAppender\">"
         +      "<param name=\"Threshold\" value=\"WARN\"/>"
         +      "<param name=\"Target\" value=\"System.out\"/>"
         +      "<layout class=\"org.apache.log4j.PatternLayout\">"
         +        " <param name=\"ConversionPattern\" value=\"%d %-5p [%c] (%t) %m%n\"/>"
         +      "</layout>"
         +    "</appender>"
         +    "<root>" 
         +       "<appender-ref ref=\"CONSOLE\"/>" 
         +	  "</root>"    
         +    "</log4j:configuration>";
      try
      {
         Element element = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.StringBufferInputStream(xmlConfig)).getDocumentElement();
         org.apache.log4j.xml.DOMConfigurator.configure(element);
      }
      catch (Exception e)
      {
         //igonre
      }
   }
}
