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
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import java.util.concurrent.TimeUnit;
import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.common.concurrent.CopyJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Base class for JBossWS test cases.
 *
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@ExtendWith(JBossWSTest.TestResultExtension.class)
public abstract class JBossWSTest extends Assertions
{
   protected static Logger log = Logger.getLogger(JBossWSTest.class.getName());
   public static final String SYSPROP_PROCESS_TIMEOUT = "test.process.wait.timeout";
   public static final String CXF_TESTS_GROUP_QUALIFIER = "cxf-tests";
   public static final String SHARED_TESTS_GROUP_QUALIFIER = "shared-tests";
   private static final int PROCESS_TIMEOUT = Integer.getInteger(SYSPROP_PROCESS_TIMEOUT, File.pathSeparatorChar == ':' ? 10 : 30); //30s on Windows, 10s on UNIX and Mac
   public JBossWSTest()
   {
   }


   /**
    * Execute <b>command</b> in separate process.
    * @param command command to execute
    * @throws IOException if I/O error occurs
    */
   public static void executeCommand(String command) throws IOException
   {
      executeCommand(command, null, null, null);
   }

   /**
    * Execute <b>command</b> in separate process. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param message message to display if assertion fails
    * @throws IOException if I/O error occurs
    */
   public static void executeCommand(String command, String message) throws IOException
   {
      executeCommand(command, null, message, null);
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @throws IOException if I/O error occurs
    */
   public static void executeCommand(String command, OutputStream os) throws IOException
   {
      executeCommand(command, os, null, null);
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @throws IOException if I/O error occurs
    */
   public static void executeCommand(String command, OutputStream os, String message) throws IOException
   {
      executeCommand(command, os, message, null);
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @param env environment
    * @throws IOException if I/O error occurs
    */
   public static void executeCommand(String command, OutputStream os, String message, Map<String, String> env) throws IOException
   {
      if (command == null)
         throw new NullPointerException( "Command cannot be null" );

      log.info("Executing command: " + command);

      StringTokenizer st = new StringTokenizer(command, " \t\r");
      List<String> tokenizedCommand = new LinkedList<String>();
      while (st.hasMoreTokens())
      {
         // PRECONDITION: command doesn't contain whitespaces in the paths
         tokenizedCommand.add(st.nextToken());
      }

      try
      {
         executeCommand(tokenizedCommand, os, message, env);
      }
      catch (IOException e)
      {
         log.warn("Make sure there are no whitespaces in command paths", e);
         throw e;
      }
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @param env environment
    * @throws IOException if I/O error occurs
    */
   private static void executeCommand(List<String> command, OutputStream os, String message, Map<String, String> env) throws IOException
   {
      ProcessBuilder pb = new ProcessBuilder(command);
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
         pb.environment().put("NOPAUSE", "true");
      }
      if (env != null) {
         for (String variable : env.keySet()) {
            pb.environment().put(variable, env.get(variable));
         }
      }
      Process p = pb.start();
      CopyJob inputStreamJob = new CopyJob(p.getInputStream(), os == null ? System.out : os);
      CopyJob errorStreamJob = new CopyJob(p.getErrorStream(), System.err);
      Thread inputJob = new Thread(inputStreamJob);
      Thread outputJob = new Thread(errorStreamJob);
      inputJob.start();
      outputJob.start();
      try {
         boolean exited = p.waitFor(PROCESS_TIMEOUT, TimeUnit.SECONDS);
         assertTrue(exited, "Process isn't exited in " + PROCESS_TIMEOUT + " seconds");
         String fallbackMessage = "Process did exit with status " + p.exitValue();
         assertTrue(p.exitValue() == 0, message != null ? message : fallbackMessage);
      } catch (InterruptedException ie) {
         ie.printStackTrace(System.err);
      } finally {
         inputStreamJob.kill();
         errorStreamJob.kill();
         p.destroy();
      }
   }

   public static MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }

   public static boolean isIntegrationCXF()
   {
      return JBossWSTestHelper.isIntegrationCXF();
   }

   public static String getServerHost()
   {
      return JBossWSTestHelper.getServerHost();
   }
   
   public static String getInitialContextFactory()
   {
      return JBossWSTestHelper.getInitialContextFactory();
   }

   public static String getRemotingProtocol()
   {
      return JBossWSTestHelper.getRemotingProtocol();
   }

   public static int getServerPort()
   {
      return JBossWSTestHelper.getServerPort();
   }

   public static int getServerPort(String groupQualifier, String containerQualifier)
   {
      return JBossWSTestHelper.getServerPort(groupQualifier, containerQualifier);
   }
   
   public static int getSecureServerPort(String groupQualifier, String containerQualifier) 
   {
	   return JBossWSTestHelper.getSecureServerPort(groupQualifier, containerQualifier);
   }

   public static File getArchiveFile(String archive)
   {
      return JBossWSTestHelper.getArchiveFile(archive);
   }

   public static URL getArchiveURL(String archive) throws MalformedURLException
   {
      return JBossWSTestHelper.getArchiveURL(archive);
   }

   public static File getResourceFile(String resource)
   {
      return JBossWSTestHelper.getResourceFile(resource);
   }

   public static URL getResourceURL(String resource) throws MalformedURLException
   {
      return JBossWSTestHelper.getResourceURL(resource);
   }

   public static File createResourceFile(String filename)
   {
      File resDir = new File(JBossWSTestHelper.getTestResourcesDir());
      return new File(resDir.getAbsolutePath() + File.separator + filename);
   }

   public static File createResourceFile(File parent, String filename)
   {
      return new File(parent, filename);
   }

   /** Get the server remote env context
    * Every test calling this method have to ensure InitialContext.close()
    * method is called at end of test to clean up all associated caches.
    */
   public static InitialContext getServerInitialContext() throws NamingException, IOException
   {
      return getServerInitialContext(null, null);
   }
   
   public static InitialContext getServerInitialContext(String groupQualifier, String containerQualifier) throws NamingException, IOException
   {
      final Hashtable<String, String> env = new Hashtable<String, String>();
      env.put("java.naming.factory.initial", getInitialContextFactory());
      env.put("java.naming.factory.url.pkgs", "org.jboss.ejb.client.naming:org.jboss.naming.remote.client");
      env.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
      env.put("jboss.naming.client.security.callback.handler.class", "org.jboss.wsf.test.CallbackHandler");
      env.put("jboss.naming.client.ejb.context", "true");
      env.put("java.naming.provider.url", getRemotingProtocol() + "://" + getServerHost() + ":" + getServerPort(groupQualifier, containerQualifier));
      return new InitialContext(env);
   }

   public static void assertEquals(Element expElement, Element wasElement, boolean ignoreWhitespace)
   {
      normalizeWhitespace(expElement, ignoreWhitespace);
      normalizeWhitespace(wasElement, ignoreWhitespace);
      String expStr = DOMWriter.printNode(expElement, false);
      String wasStr = DOMWriter.printNode(wasElement, false);
      if (expStr.equals(wasStr) == false)
      {
         System.out.println("\nExp: " + expStr + "\nWas: " + wasStr);
      }
      assertEquals(expStr, wasStr);
   }

   public static void assertEquals(Element expElement, Element wasElement)
   {
      assertEquals(expElement, wasElement, false);
   }

   public static void assertEquals(Object exp, Object was)
   {
      if (exp instanceof Object[] && was instanceof Object[])
         assertArrayEquals((Object[])exp, (Object[])was);
      else if (exp instanceof byte[] && was instanceof byte[])
         assertArrayEquals((byte[])exp, (byte[])was);
      else if (exp instanceof boolean[] && was instanceof boolean[])
         assertArrayEquals((boolean[])exp, (boolean[])was);
      else if (exp instanceof short[] && was instanceof short[])
         assertArrayEquals((short[])exp, (short[])was);
      else if (exp instanceof int[] && was instanceof int[])
         assertArrayEquals((int[])exp, (int[])was);
      else if (exp instanceof long[] && was instanceof long[])
         assertArrayEquals((long[])exp, (long[])was);
      else if (exp instanceof float[] && was instanceof float[])
         assertArrayEquals((float[])exp, (float[])was);
      else if (exp instanceof double[] && was instanceof double[])
         assertArrayEquals((double[])exp, (double[])was);
      else
         Assertions.assertEquals(exp, was);
   }
   /*protected static void assertTrue(String reason, boolean condition) {
      Assertions.assertTrue(condition, reason);
   }

   protected static void assertFalse(String reason, boolean condition) {
      Assertions.assertFalse(condition, reason);
   }

   protected static void assertEquals(String message, char expected, char actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, String expected, String actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, Object expected, Object actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, double expected, double actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, long expected, long actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, int expected, int actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, byte expected, byte actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertEquals(String message, float expected, float actual) {
      Assertions.assertEquals(expected, actual, message);
   }

   protected static void assertNotNull(String message, Object obj) {
      Assertions.assertNotNull(obj, message);
   }

   protected static void assertNotNull(String message, String obj) {
      Assertions.assertNotNull(obj, message);
   }*/

   /** Removes whitespace text nodes if they have an element sibling.
    */
   private static void normalizeWhitespace(Element element, boolean ignoreWhitespace)
   {
      boolean hasChildElement = false;
      ArrayList<Node> toDetach = new ArrayList<Node>();

      NodeList childNodes = element.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         Node node = childNodes.item(i);
         if (node.getNodeType() == Node.TEXT_NODE)
         {
            String nodeValue = node.getNodeValue();
            if (nodeValue.trim().length() == 0)
               toDetach.add(node);
         }
         if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            normalizeWhitespace((Element)node, ignoreWhitespace);
            hasChildElement = true;
         }
      }

      // remove whitespace nodes
      if (hasChildElement || ignoreWhitespace)
      {
         Iterator<Node> it = toDetach.iterator();
         while (it.hasNext())
         {
            Node whiteSpaceNode = it.next();
            element.removeChild(whiteSpaceNode);
         }
      }
   }

   public static class TestResultExtension implements TestWatcher, BeforeEachCallback, AfterEachCallback {

      private ClassLoader classLoader = null;

      @Override
      public void afterEach(ExtensionContext ctx) throws Exception {
         if (classLoader != null && ctx.getElement().isPresent() && ctx.getElement().get().isAnnotationPresent(WrapThreadContextClassLoader.class)) {
            Thread.currentThread().setContextClassLoader(classLoader);
         }
      }

      @Override
      public void beforeEach(ExtensionContext ctx) throws Exception {
         final Method cjpMethod;
         try {
           cjpMethod = ctx.getRequiredTestClass().getDeclaredMethod("getClientJarPaths");
           cjpMethod.setAccessible(true);
         } catch (NoSuchMethodException nme) {
            return;
         }
         final String cjp = cjpMethod.invoke(ctx.getRequiredTestInstance()).toString();
         if (cjp == null || cjp.trim().isEmpty()) {
            return;
         }
         if (ctx.getElement().isPresent() && ctx.getElement().get().isAnnotationPresent(WrapThreadContextClassLoader.class)) {
            classLoader = Thread.currentThread().getContextClassLoader();

            StringTokenizer st = new StringTokenizer(cjp, ", ");
            URL[] archives = new URL[st.countTokens()];

            try {
               for (int i = 0; i < archives.length; i++)
                  archives[i] = new File(JBossWSTestHelper.getTestArchiveDir(), st.nextToken()).toURI().toURL();

               URLClassLoader cl = new URLClassLoader(archives, classLoader);
               Thread.currentThread().setContextClassLoader(cl);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
      }
   }

   protected String getClientJarPaths() {
      return null;
   }
}