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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.api.tools.WSContractProvider;
import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Element;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class WSProviderPlugin extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   // tools delegate
   WSContractProvider provider;

   // redirect tools message to System.out ?
   boolean toogleMessageOut = Boolean.getBoolean(WSProviderPlugin.class.getName()+".verbose");

   // relative to test execution
   File outputDirectory;

   public WSProviderPlugin()
   {
       // create a new consumer for every test case
      provider = WSContractProvider.newInstance();
      if(toogleMessageOut) provider.setMessageStream(System.out);

      // shared output directory, relative to test execution
      outputDirectory = createResourceFile(".." + FS + "wsprovide" + FS + "java");      
   }

   private ClassLoader getArtefactClassLoader() throws Exception {
      URLClassLoader loader = new URLClassLoader(
        new URL[] { outputDirectory.toURI().toURL() },
        Thread.currentThread().getContextClassLoader()
      );

      return loader;
   }

   /**
    * Enables/Disables WSDL generation.
    *
    */
   public void testGenerateWsdl() throws Exception
   {
      provider.setGenerateWsdl(true);
      provide();

      verifyWSDL(outputDirectory);
   }

   /**
    * Enables/Disables WSDL generation.
    *
    */
   public void testGenerateWsdlWithExtension() throws Exception
   {
      provider.setGenerateWsdl(true);
      provider.setExtension(true);
      final String portSoapAddress = "http://www.jboss.org/myEp";
      provider.setPortSoapAddress(portSoapAddress); //also check portSoapAddress option
      File outputDir = new File(outputDirectory.getAbsolutePath() + "-soap12"); 
      provide(outputDir);

      verifyWSDL(outputDir, true, portSoapAddress);
   }

   /**
    * Enables/Disables Java source generation.
    *
    */
   public void testGenerateSource() throws Exception
   {
      provider.setGenerateSource(true);
      provide();

      verifyJavaSource(outputDirectory);

   }

   private void verifyJavaSource(File directory) throws Exception
   {
      File javaSource = new File(
        directory.getAbsolutePath()+
          FS + "org" + FS + "jboss" + FS + "test" + FS + "ws" + FS + "jaxws" + FS + "smoke" + FS + "tools" + FS + "jaxws" + FS + "AddResponse.java"
        );

      assertTrue("Source not generated", javaSource.exists());
      
      javaSource = new File(directory.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "ws" + FS + "jaxws" + FS + "smoke" + FS + "tools" + FS
            + "jaxws" + FS + "GetKeysResponse.java");
      assertTrue("Source not generated", javaSource.exists());
      String contents = readFile(javaSource);
      
      //[JBWS-2477] check support for generics
      boolean bool = contents.contains("public Set<Integer> getReturn()") || contents.contains(" public java.util.Set<java.lang.Integer> getReturn()");
      assertTrue("Didn't found method \"public Set<Integer> getReturn()\"", bool);
      
      javaSource = new File(directory.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "ws" + FS + "jaxws" + FS + "smoke" + FS + "tools" + FS
            + "jaxws" + FS + "ProcessListResponse.java");
      assertTrue("Source not generated", javaSource.exists());
      contents = readFile(javaSource);
      assertTrue("@XmlList not found", contents.contains("@XmlList"));
      
   }
   
   private String readFile(File file) throws Exception
   {
      BufferedReader input = new BufferedReader(new FileReader(file));
      StringBuilder sb = new StringBuilder();
      try
      {
         String line = null;
         while ((line = input.readLine()) != null)
         {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
         }
      }
      finally
      {
         input.close();
      }
      return sb.toString();
   }

   /**
    * Sets the main output directory.
    * If the directory does not exist, it will be created.
    */
   public void testOutputDirectory() throws Exception
   {
      provide();
      ClassLoader loader = getArtefactClassLoader();
      Class<?> responseWrapper = loader.loadClass("org.jboss.test.ws.jaxws.smoke.tools.jaxws.AddResponse");
      XmlRootElement rootElement = (XmlRootElement) responseWrapper.getAnnotation(XmlRootElement.class);
      assertNotNull("@XmlRootElement missing from response wrapper", rootElement);
      assertEquals("Wrong namespace", rootElement.namespace(), "http://foo.bar.com/calculator");
      responseWrapper = loader.loadClass("org.jboss.test.ws.jaxws.smoke.tools.jaxws.ProcessListResponse");
      XmlList xmlList = (XmlList) responseWrapper.getDeclaredField("_return").getAnnotation(XmlList.class);
      assertNotNull("@XmlList missing from response wrapper's _return field", xmlList);
      responseWrapper = loader.loadClass("org.jboss.test.ws.jaxws.smoke.tools.jaxws.ProcessCustomResponse");
      XmlJavaTypeAdapter xmlJavaTypeAdapter = (XmlJavaTypeAdapter)responseWrapper.getDeclaredField("_return").getAnnotation(XmlJavaTypeAdapter.class);
      assertNotNull("@XmlJavaTypeAdapter missing from response wrapper's _return field", xmlJavaTypeAdapter);
      assertEquals("org.jboss.test.ws.jaxws.smoke.tools.CustomAdapter", xmlJavaTypeAdapter.value().getName());
   }

   /**
    * Sets the resource directory. This directory will contain any generated
    * WSDL and XSD files. If the directory does not exist, it will be created.
    * If not specified, the output directory will be used instead.
    *
    */
   public void testResourceDirectory() throws Exception
   {
      File directory = createResourceFile("wsprovide" + FS + "resources");
      provider.setResourceDirectory(directory);
      provide();

      verifyWSDL(outputDirectory);
   }
   
   private void verifyWSDL(File directory) throws Exception
   {
      this.verifyWSDL(directory, false, null);
   }

   private void verifyWSDL(File directory, boolean soap12, String portSoapAddress) throws Exception
   {
      File wsdl = new File(
        directory.getAbsolutePath()+
          FS + "CalculatorBeanService.wsdl"
      );

      assertTrue("WSDL not generated", wsdl.exists());
      Element root = DOMUtils.parse( new FileInputStream(wsdl));
      Element serviceElement = DOMUtils.getFirstChildElement(root, "service");
      assertEquals(serviceElement.getAttribute("name"), "CalculatorBeanService");
      Element bindingElement = DOMUtils.getFirstChildElement(root, "binding");
      Element soapBindingElement = DOMUtils.getFirstChildElement(bindingElement,"binding");
      if (soap12)
      {
         assertEquals("http://schemas.xmlsoap.org/wsdl/soap12/", soapBindingElement.getNamespaceURI());
      }
      else
      {
         assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", soapBindingElement.getNamespaceURI());
      }
      
      if (portSoapAddress != null) {
         Element portElement = DOMUtils.getFirstChildElement(serviceElement, "port");
         Element addressElement = DOMUtils.getFirstChildElement(portElement, "address");
         assertEquals(portSoapAddress, addressElement.getAttribute("location"));
      }
   }

   /**
    * Sets the source directory. This directory will contain any generated Java source.
    * If the directory does not exist, it will be created. If not specified,
    * the output directory will be used instead.
    *
    */
   public void testSourceDirectory() throws Exception
   {
      File sourceDir = createResourceFile("wsprovide" + FS + "sources");
      provider.setSourceDirectory(sourceDir);
      provider.setGenerateSource(true);
      provide();

      verifyJavaSource(sourceDir);
   }

   /**
    * Sets the ClassLoader used to discover types.
    * This defaults to the one used in instantiation.
    *
    */
   public void testClassLoader() throws Exception
   {
      // Work around the sure jre settings
      String javaHome = System.getProperty("java.home");
      int jreIdx = javaHome.indexOf(FS + "jre");
      URLClassLoader loader;
      if (jreIdx > 0) {
          String jdkHome = javaHome.substring(0, jreIdx);
          String targetDir = createResourceFile("").getParent();
          loader = new URLClassLoader(
             new URL[]
             {
                new URL("file:"+targetDir+FS+"test-libs" + FS + "jaxws-classloading-service.jar"),
                new URL("file:"+targetDir+FS+"test-libs" + FS + "jaxws-classloading-types.jar"),
                new URL("file:"+jdkHome+FS+"lib" + FS + "tools.jar")
             },
            getArtefactClassLoader()
          );
      } else {
          String targetDir = createResourceFile("").getParent();
          loader = new URLClassLoader(
             new URL[]
             {
                new URL("file:"+targetDir+FS+"test-libs" + FS + "jaxws-classloading-service.jar"),
                new URL("file:"+targetDir+FS+"test-libs" + FS + "jaxws-classloading-types.jar"),
             },
            getArtefactClassLoader()
          );
      }

      provider.setClassLoader(loader);
      provider.setGenerateWsdl(true);
      provider.setOutputDirectory(outputDirectory);
      provider.provide("org.jboss.test.ws.jaxws.smoke.tools.service.HelloWorld");

      File wsdl = new File(outputDirectory.getAbsolutePath() + FS + "HelloWorldService.wsdl");

      assertTrue("WSDL not generated", wsdl.exists());
      Element root = DOMUtils.parse( new FileInputStream(wsdl));
      Element serviceElement = DOMUtils.getFirstChildElement(root, "service");
      assertEquals(serviceElement.getAttribute("name"), "HelloWorldService");
   }

   /**
    * Sets the PrintStream to use for status feedback. The simplest example
    * would be to use System.out.
    *
    */
   public void testMessageStream() throws Exception
   {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      PrintStream pout = new PrintStream(bout);

      provider.setMessageStream(pout);
      provide();

      String messageOut = new String(bout.toByteArray());

      System.out.println("-- Begin captured output --");
      System.out.println(messageOut);
      System.out.println("-- End captured output --");

      if (isIntegrationCXF())
      {
         assertTrue("Provider messages not correctly redirected", messageOut.indexOf("java2ws -s") != -1 );
      }
      else
      {
         assertTrue("Provider messages not correctly redirected",
           messageOut.replace('\\', '/').indexOf("org/jboss/test/ws/jaxws/smoke/tools/jaxws/Add.class") != -1 );
      }
   }

   private void provide() throws Exception
   {
      this.provide(outputDirectory);
   }
   
   private void provide(File outputDirectory)
   {
      provider.setOutputDirectory(outputDirectory);
      provider.provide(CalculatorBean.class);
   }
}
