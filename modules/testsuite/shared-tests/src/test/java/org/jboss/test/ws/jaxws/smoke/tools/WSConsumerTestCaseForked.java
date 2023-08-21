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
package org.jboss.test.ws.jaxws.smoke.tools;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the WSContractConsumer API across different implementations.
 * 
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class WSConsumerTestCaseForked extends PluginBase
{

   /**
    * Recreates a tools delegate for every test
    * @throws Exception
    */
   @Before
   public void setup() throws Exception
   {
      // JBWS-3937 (IBM JDK 8)
      System.setProperty("javax.xml.accessExternalSchema", "file,http");
      // JBWS-2175
      setupClasspath();

      Class<?> wscClass = Thread.currentThread().getContextClassLoader().loadClass(WSConsumerPlugin.class.getName());
      setDelegate(wscClass);
   }

   @After
   public void teardown() throws Exception
   {
      restoreClasspath();
   }

   /**
    * Specifies the JAX-WS and JAXB binding files to use on import operations.
    * See http://java.sun.com/webservices/docs/2.0/jaxws/customizations.html
    */
   @Test
   @RunAsClient
   public void testBindingFiles() throws Exception
   {
      dispatch("testBindingFiles");
   }

   /**
    * Sets the OASIS XML Catalog file to use for entity resolution.
    *
    */
   @Test
   @RunAsClient
   public void testCatalog() throws Exception
   {
      dispatch("testCatalog");
   }

   /**
    * Sets the main output directory. If the directory does not exist, it will be created.                        org.jboss.test.ws.jaxws.smoke.tools
    *
    */
   @Test
   @RunAsClient
   public void testOutputDirectory() throws Exception
   {
      dispatch("testOutputDirectory");
   }

   /**
    * Sets the source directory. This directory will contain any generated Java source.
    * If the directory does not exist, it will be created. If not specified,
    * the output directory will be used instead.
    *
    */
   @Test
   @RunAsClient
   public void testSourceDirectory() throws Exception
   {
      dispatch("testSourceDirectory");
   }
   
   
   /**
    * If there are "-n" and "-s" flag, with "-k", the generated
    * artifacts should be placed in source directory 
    */
   @Test
   @RunAsClient
   public void testNoCompile() throws Exception
   {
      dispatch("testNoCompile");
   }

   /**
    * If there are "-n" and "-s" flag, without "-k", nothing should be generated 
    */
   @Test
   @RunAsClient
   public void testNoCompileNoKeep() throws Exception
   {
      dispatch("testNoCompileNoKeep");
   }

   /**
    * Enables/Disables Java source generation.
    *
    */
   @Test
   @RunAsClient
   public void testGenerateSource() throws Exception
   {
      dispatch("testGenerateSource");
   }

   /**
    * Sets the target package for generated source. If not specified the default
    * is based off of the XML namespace.
    *    
    */
   @Test
   @RunAsClient
   public void testTargetPackage() throws Exception
   {
      dispatch("testTargetPackage");
   }

   /**
    * Sets the @@WebService.wsdlLocation and @@WebServiceClient.wsdlLocation attributes to a custom value.
    *
    */
   @Test
   @RunAsClient
   public void testWsdlLocation() throws Exception
   {
      dispatch("testWsdlLocation");
   }

   /**
    * Sets the PrintStream to use for status feedback.
    * The simplest example would be to use System.out.      
    */
   @Test
   @RunAsClient
   public void testMessageStream() throws Exception
   {
      dispatch("testMessageStream");
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
   @Test
   @RunAsClient
   public void testTarget() throws Exception
   {
      dispatch("testTarget");
   }

   /**
    * Tests the SOAP 1.2 binding extension
    *
    */
   @Test
   @RunAsClient
   public void testSOAP12Extension() throws Exception
   {
      dispatch("testSOAP12Extension");
   }
   
   /**
    * Test the implicit header generation support
    */
   @Test
   @RunAsClient
   public void testAdditionalHeaders() throws Exception
   {
      dispatch("testAdditionalHeaders");
   }
}