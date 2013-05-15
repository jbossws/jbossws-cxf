/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.smoke.tools;

import org.jboss.ws.api.tools.WSContractConsumer;
import org.jboss.wsf.test.JBossWSTest;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceFeature;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 * @author alessio.soldano@jboss.com
 */
public class WSConsumerPlugin extends JBossWSTest implements StackConfigurable
{
   // Tools delegate. Recreated for every test. See setup(...)
   WSContractConsumer consumer;

   // common output dir for all tests. Tests need to be executed below 'output/tests'
   File outputDirectory;

   // default is off
   boolean toogleMessageOut = Boolean.getBoolean(WSConsumerPlugin.class.getName()+".verbose");

   private File workDirectory;
   
   protected boolean integrationNative;
   protected boolean integrationMetro;
   protected boolean integrationCXF;


   public WSConsumerPlugin()
   {
      // create a new consumer for every test case
      consumer = WSContractConsumer.newInstance();
      consumer.setNoCompile(true);
      
      if (toogleMessageOut)
      {
         consumer.setMessageStream(System.out);
      }

      // shared output directory, we go out of the test-resources directory
      outputDirectory = createResourceFile("../wsconsume/java");
      workDirectory = createResourceFile("../work");
   }  

   /**
    * Specifies the JAX-WS and JAXB binding files to use on import operations.
    * See http://java.sun.com/webservices/docs/2.0/jaxws/customizations.html
    */
   public void testBindingFiles() throws Exception
   {
      List<File> files = new ArrayList<File>();
      files.add(getResourceFile("jaxws/smoke/tools/wsdl/async-binding.xml"));

      consumer.setBindingFiles(files);
      consumer.setTargetPackage("org.jboss.test.ws.tools.testBindingFiles");
      consumer.setGenerateSource(true);

      consumeWSDL();

      File sei = loadEndpointInterface("testBindingFiles");
     
      boolean containsAsyncOperations = false;
      BufferedReader bin = new BufferedReader( new FileReader(sei) );

      String l = bin.readLine();
      while(l!=null)
      {
         if(l.indexOf("echoAsync")!=-1)
         {
            containsAsyncOperations=true;
            break;
         }

         l = bin.readLine();
      }

      assertTrue("External binding file was ignored", containsAsyncOperations);

   }

   /**
    * Sets the OASIS XML Catalog file to use for entity resolution.
    *
    */
   public void testCatalog() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testCatalog");
      consumer.setCatalog(getResourceFile("jaxws/smoke/tools/wsdl/jax-ws-catalog.xml"));
      consumer.setGenerateSource(true);
      consumer.setOutputDirectory(outputDirectory);
      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestServiceCatalog.wsdl").getCanonicalPath());
   }

   /**
    * Sets the main output directory. If the directory does not exist, it will be created.
    *
    */
   public void testOutputDirectory() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testOutputDirectory");
      consumer.setGenerateSource(true);
      consumer.setSourceDirectory(new File(workDirectory, "testOutputDirectory/java/"));

      consumeWSDL();

      File sei = new File(workDirectory, "testOutputDirectory/java/org/jboss/test/ws/tools/testOutputDirectory/EndpointInterface.java");
      assertTrue("Output directory switch ignored", sei.exists());
   }

   /**
    * Sets the source directory. This directory will contain any generated Java source.
    * If the directory does not exist, it will be created. If not specified,
    * the output directory will be used instead.
    *
    */
   public void testSourceDirectory() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testSourceDirectory");
      consumer.setGenerateSource(true);
      consumer.setSourceDirectory(new File(workDirectory, "wsconsumeSource/java/"));

      consumeWSDL();

      File sei = new File(workDirectory, "wsconsumeSource/java/org/jboss/test/ws/tools/testSourceDirectory/EndpointInterface.java");
      assertTrue("Source directory switch ignored", sei.exists());
   }

   public void testNoCompile() throws Exception
   {
      File sourceDir = new File(workDirectory, "wsconsumeNoCPSources/java/");
      File outputDir = new File(workDirectory, "wsconsumeNoCPOutput/java/");
      consumer.setTargetPackage("org.jboss.test.ws.tools.testSourceDirectory");
      consumer.setSourceDirectory(sourceDir);
      consumer.setOutputDirectory(outputDir);
      consumer.setGenerateSource(true);

      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestService.wsdl").getCanonicalPath());

      File sei = new File(workDirectory, "wsconsumeNoCPSources/java/org/jboss/test/ws/tools/testSourceDirectory/EndpointInterface.java");
      assertTrue("Expected sei not generated in the expected directory " + outputDir.getPath() , sei.exists());
      
      File notExistSei = new File(workDirectory, "wsconsumeNoCPOutput/java/org/jboss/test/ws/tools/testSourceDirectory/EndpointInterface.java");
      assertFalse("Directory " + sourceDir.getPath() + "  is expected to empty", notExistSei.exists());
   }
   
   public void testNoCompileNoKeep() throws Exception
   {
      File sourceDir = new File(workDirectory, "wsconsumeNoCPNoKeepsource/java/");
      File outputDir = new File(workDirectory, "wsconsumeNoCPNoKeepOutput/java/");
      consumer.setTargetPackage("org.jboss.test.ws.tools.testSourceDirectory");
      consumer.setSourceDirectory(sourceDir);
      consumer.setOutputDirectory(outputDir);
      consumer.setGenerateSource(false);

      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestService.wsdl").getCanonicalPath());

      File sourceSei = new File(workDirectory, "wsconsumeNoCPNoKeepsource/java/org/jboss/test/ws/tools/testSourceDirectory/EndpointInterface.java");
      assertFalse("Directory " + sourceDir.getPath() + "  is expected to be empty", sourceSei.exists());
      
      File outputSei = new File(workDirectory, "wsconsumeNoCPNoKeepOutput/java/org/jboss/test/ws/tools/testSourceDirectory/EndpointInterface.java");
      assertFalse("Directory " + sourceDir.getPath() + "  is expected to be empty", outputSei.exists());
   }
   
   
   /**
    * Enables/Disables Java source generation.
    *
    */
   public void testGenerateSource() throws Exception
   {
      File sourceDir = new File(workDirectory, "wsconsumeGenerateSource/java/");
      consumer.setTargetPackage("org.jboss.test.ws.tools.testGenerateSource");
      consumer.setSourceDirectory(sourceDir);
      consumer.setGenerateSource(true);
      consumer.setNoCompile(true);

      consumeWSDL();

      File packageDir = new File(sourceDir, "org/jboss/test/ws/tools/testGenerateSource");
      assertTrue("Package not created", packageDir.exists());

      File seiSource = new File(sourceDir, "org/jboss/test/ws/tools/testGenerateSource/EndpointInterface.java");
      assertTrue("SEI not generated", seiSource.exists());
      
      sourceDir = new File(workDirectory, "wsconsumeGenerateSource2/java/");
      consumer.setTargetPackage("org.jboss.test.ws.tools.testGenerateSource2");
      consumer.setSourceDirectory(sourceDir);
      consumer.setGenerateSource(false);
      consumer.setNoCompile(false);

      consumeWSDL();

      packageDir = new File(sourceDir, "org/jboss/test/ws/tools/testGenerateSource2");
      assertFalse("Package should not have been created!", packageDir.exists());

      File interfaceClass = new File(outputDirectory, "org/jboss/test/ws/tools/testGenerateSource2/EndpointInterface.class");
      assertTrue("SEI not generated", interfaceClass.exists());
   }

   /**
    * Sets the target package for generated source. If not specified the default
    * is based off of the XML namespace.
    *
    */
   public void testTargetPackage() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testTargetPackage");
      consumer.setGenerateSource(true);

      consumeWSDL();

      File packageDir = new File(outputDirectory, "org/jboss/test/ws/tools/testTargetPackage");
      assertTrue("Package not created", packageDir.exists());

      File seiSource = new File(outputDirectory, "org/jboss/test/ws/tools/testTargetPackage/EndpointInterface.java");
      assertTrue("SEI not generated", seiSource.exists());
   
      File seiClass = loadEndpointInterface("testTargetPackage");
      assertTrue("Cannot load SEI class", seiClass.exists());
   }

   /**
    * Sets the @@WebService.wsdlLocation and @@WebServiceClient.wsdlLocation attributes to a custom value.
    *
    */
   public void testWsdlLocation() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testWsdlLocation");
      consumer.setWsdlLocation("http://foo.bar.com/endpoint?wsdl");
      consumer.setGenerateSource(true);     

      consumeWSDL();

      File sei = loadEndpointInterface("testWsdlLocation", "TestService.java");
      BufferedReader bin = new BufferedReader( new FileReader(sei) );
      
      boolean match = false;
      boolean annotationFound = false;
      String l = bin.readLine();
      while(l!=null)
      {
         if (l.startsWith("@WebServiceClient"))
         {
            annotationFound = true;
         }
         if (l.indexOf("public class TestService")!=-1 && annotationFound)
         {
            match = true;
            break;
         }
         l = bin.readLine();
      }
     
      assertTrue("@WebServiceClient not generated on service interface", match);      
   }

   /**
    * Sets the PrintStream to use for status feedback.
    * The simplest example would be to use System.out.
    */
   public void testMessageStream() throws Exception
   {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      PrintStream pout = new PrintStream(bout);

      consumer.setTargetPackage("org.jboss.test.ws.tools.testMessageStream");
      consumer.setMessageStream(pout);

      consumeWSDL();

      String messageOut = new String(bout.toByteArray());
      System.out.println("-- Begin captured output -- ");
      System.out.println(messageOut);
      System.out.println("--- End captured output --");

      if (getIsCXF())
      {
         assertTrue("Tools output not correctly redirected", messageOut.indexOf("wsdl2java -exsh false -p org.jboss.test.ws.tools.testMessageStream") != -1);
      }
      else
      {
         assertTrue("Tools output not correctly redirected",
           messageOut.replace('\\', '/').indexOf("org/jboss/test/ws/tools/testMessageStream/EndpointInterface.java")!=-1
         );
      }
   }

   /**
    * Sets the additional classpath to use if/when invoking the Java compiler.
    * Typically an implementation will use the system <code>java.class.path</code>
    * property. So for most normal applications this method is not needed. However,
    * if this API is being used from an isolated classloader, then it needs to
    * be called in order to reference all jars that are required by the
    * implementation.
    *
    */
   public void testAdditionalCompilerClassPath()
   {
      // JBWS-1773 WSContractConsumer.setAdditionalCompilerClassPath() method is tested in wsconsume ant task
      // that is invoked on each test run. See WSConsumeTask.java for more information how this is tested.
   }

   /**
    * Set the target JAX-WS specification target. Defaults to <code>2.0</code>
    */
   public void testTarget() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testTarget");
      consumer.setGenerateSource(true);
      consumer.setTarget("2.1");
      consumer.setNoCompile(false);
      
      consumeWSDL();
      ClassLoader loader = getArtefactClassLoader();
      Class<?> service = loader.loadClass("org.jboss.test.ws.tools.testTarget.TestService");

      boolean featureSig = false;
      for (Method m : service.getDeclaredMethods())
      {
         if (m.getName().equals("getEndpointInterfacePort"))
         {
            for (Class<?> c : m.getParameterTypes())
            {
               if (c.isArray() && c.getComponentType().equals(WebServiceFeature.class))
               {
                  featureSig = true;
                  break;
               }
            }
         }
      }

      assertTrue("JAX-WS 2.1 extensions not generated with 'target=2.1'", featureSig);
      
      Class<?> sei = loader.loadClass("org.jboss.test.ws.tools.testTarget.EndpointInterface");
      assertTrue("@XmlSeeAlso expected on SEI (types not referenced by the Port in the wsdl)", sei.isAnnotationPresent(XmlSeeAlso.class));
      
      boolean featureConstructor = false;
      for (Constructor<?> c : service.getConstructors()) {
         for (Class<?> pt : c.getParameterTypes())
         {
            if (pt.isArray() && pt.getComponentType().equals(WebServiceFeature.class)) {
               featureConstructor = true;
               break;
            }
         }
      }
      assertFalse("Found JAXWS 2.2 constructor", featureConstructor);
   }

   /**
    * Tests the SOAP 1.2 binding extension
    *
    */
   public void testSOAP12Extension() throws Exception
   {
      consumer.setOutputDirectory(outputDirectory);
      consumer.setTargetPackage("org.jboss.test.ws.tools.testSOAP12Extension");
      consumer.setGenerateSource(true);
      consumer.setExtension(true);
      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestServiceSoap12.wsdl").getCanonicalPath());

      File sei = new File(outputDirectory, "org/jboss/test/ws/tools/testSOAP12Extension/EndpointInterface.java");
      assertTrue("SEI not generated", sei.exists());
      File service = new File(outputDirectory, "org/jboss/test/ws/tools/testSOAP12Extension/TestService.java");
      assertTrue("Service not generated", service.exists());
   }
   
   public void testAdditionalHeaders() throws Exception
   {
      consumer.setTargetPackage("org.jboss.test.ws.tools.testAdditionalHeaders1");
      consumer.setAdditionalHeaders(false);
      consumer.setNoCompile(false);
      consumeWSDL();
      ClassLoader loader = getArtefactClassLoader();
      Class<?> sei = loader.loadClass("org.jboss.test.ws.tools.testAdditionalHeaders1.EndpointInterface");
      Method m = (sei.getMethods())[0];
      assertEquals(1, m.getParameterTypes().length);
      consumer.setOutputDirectory(outputDirectory);
      consumer.setTargetPackage("org.jboss.test.ws.tools.testAdditionalHeaders2");
      consumer.setAdditionalHeaders(true);
      consumer.setNoCompile(false);
      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestServiceImplicitHeader.wsdl").getCanonicalPath());
      loader = getArtefactClassLoader();
      sei = loader.loadClass("org.jboss.test.ws.tools.testAdditionalHeaders2.EndpointInterface");
      m = (sei.getMethods())[0];
      assertEquals(2, m.getParameterTypes().length);
   }

   private void consumeWSDL() throws Exception
   {
      consumer.setOutputDirectory(outputDirectory);
      consumer.consume(getResourceFile("jaxws/smoke/tools/wsdl/TestService.wsdl").getCanonicalPath());
   }
   
   private File loadEndpointInterface(String testName, String... fileName) throws MalformedURLException, ClassNotFoundException
   {
      String name = fileName.length> 0 ? fileName[0] : "EndpointInterface.java";
      String interfaceFile = "org/jboss/test/ws/tools/" + testName + "/"+name;
      File sei = new File(outputDirectory, interfaceFile);
      if(!sei.exists()) throw new IllegalStateException(sei.getAbsolutePath() + " doesn't exist!");
      return sei;
   }
   
   private ClassLoader getArtefactClassLoader() throws Exception {
      URLClassLoader loader = new URLClassLoader(
        new URL[] { outputDirectory.toURI().toURL() },
        Thread.currentThread().getContextClassLoader()
      );

      return loader;
   }
   
   public boolean getIsNative()
   {
      return integrationNative;
   }

   public boolean getIsCXF()
   {
      return integrationCXF;
   }

   public void setIsNative(boolean integrationNative)
   {
      this.integrationNative = integrationNative;
   }

   public void setIsCXF(boolean integrationCXF)
   {
      this.integrationCXF = integrationCXF;
   }
}
