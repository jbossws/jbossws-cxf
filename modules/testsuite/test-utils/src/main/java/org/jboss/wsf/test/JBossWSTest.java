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
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.common.concurrent.CopyJob;
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
public abstract class JBossWSTest extends TestCase
{
   protected static Logger log = Logger.getLogger(JBossWSTest.class.getName());

   public JBossWSTest()
   {
   }

   public JBossWSTest(String name)
   {
      super(name);
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
      if (env != null)
      {
         for (String variable : env.keySet())
         {
            pb.environment().put(variable, env.get(variable));
         }
      }
      Process p = pb.start();
      CopyJob inputStreamJob = new CopyJob(p.getInputStream(), os == null ? System.out : os);
      CopyJob errorStreamJob = new CopyJob(p.getErrorStream(), System.err);
      // unfortunately the following threads are needed because of Windows behavior
      System.out.println("Process input stream:");
      System.err.println("Process error stream:");
      Thread inputJob = new Thread(inputStreamJob);
      Thread outputJob = new Thread(errorStreamJob);
      try
      {  
         inputJob.start();
         inputJob.join(60000);
         outputJob.start();
         outputJob.join(60000);
         int statusCode = p.waitFor();
         String fallbackMessage = "Process did exit with status " + statusCode; 
         assertTrue(message != null ? message : fallbackMessage, statusCode == 0);
      }
      catch (InterruptedException ie)
      {
         ie.printStackTrace(System.err);
      }
      finally
      {
         inputStreamJob.kill();
         errorStreamJob.kill();
         p.destroy();
      }
   }

   public static MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }

   public static boolean isTargetJBoss7()
   {
      return JBossWSTestHelper.isTargetJBoss7();
   }

   public static boolean isTargetJBoss71()
   {
      return JBossWSTestHelper.isTargetJBoss71();
   }

   public static boolean isTargetJBoss72()
   {
      return JBossWSTestHelper.isTargetJBoss72();
   }

   public static boolean isIntegrationNative()
   {
      return JBossWSTestHelper.isIntegrationNative();
   }

   public static boolean isIntegrationCXF()
   {
      return JBossWSTestHelper.isIntegrationCXF();
   }

   public static void deploy(String archive) throws Exception
   {
      JBossWSTestHelper.deploy(archive);
   }

   public static void undeploy(String archive) throws Exception
   {
      JBossWSTestHelper.undeploy(archive);
   }

   public static String getServerHost()
   {
      return JBossWSTestHelper.getServerHost();
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

   private static Hashtable<String, String> getEnvironment(final String resourceName) throws IOException {
       final Hashtable<String, String> env = new Hashtable<String, String>();
       final InputStream is = JBossWSTest.class.getClassLoader().getResourceAsStream(resourceName);
       if (is != null) {
           final Properties props = new Properties();
           props.load(is);
           Entry<Object, Object> entry;
           final Iterator<Entry<Object, Object>> entries = props.entrySet().iterator();
           while (entries.hasNext()) {
               entry = entries.next();
               env.put((String)entry.getKey(), (String)entry.getValue());
           }
       }
       return env;
   }

   /** Gets the appclient remote env context.
    * Every test calling this method have to ensure InitialContext.close()
    * method is called at end of test to clean up all associated caches.
    */
   public static InitialContext getAppclientInitialContext() throws NamingException, IOException
   {
       final Hashtable<String, String> env = getEnvironment("appclient.jndi.properties");
       return new InitialContext(env);
   }

   /** Get the server remote env context
    * Every test calling this method have to ensure InitialContext.close()
    * method is called at end of test to clean up all associated caches.
    */
   public static InitialContext getServerInitialContext() throws NamingException, IOException
   {
       final Hashtable<String, String> env = getEnvironment("server.jndi.properties");
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
         assertEqualsArray((Object[])exp, (Object[])was);
      else if (exp instanceof byte[] && was instanceof byte[])
         assertEqualsArray((byte[])exp, (byte[])was);
      else if (exp instanceof boolean[] && was instanceof boolean[])
         assertEqualsArray((boolean[])exp, (boolean[])was);
      else if (exp instanceof short[] && was instanceof short[])
         assertEqualsArray((short[])exp, (short[])was);
      else if (exp instanceof int[] && was instanceof int[])
         assertEqualsArray((int[])exp, (int[])was);
      else if (exp instanceof long[] && was instanceof long[])
         assertEqualsArray((long[])exp, (long[])was);
      else if (exp instanceof float[] && was instanceof float[])
         assertEqualsArray((float[])exp, (float[])was);
      else if (exp instanceof double[] && was instanceof double[])
         assertEqualsArray((double[])exp, (double[])was);
      else
         TestCase.assertEquals(exp, was);
   }

   private static void assertEqualsArray(Object[] exp, Object[] was)
   {
      if (exp == null && was == null)
         return;

      if (exp != null && was != null)
      {
         if (exp.length != was.length)
         {
            fail("Expected <" + exp.length + "> array items, but was <" + was.length + ">");
         }
         else
         {
            for (int i = 0; i < exp.length; i++)
            {

               Object compExp = exp[i];
               Object compWas = was[i];
               assertEquals(compExp, compWas);
            }
         }
      }
      else if (exp == null)
      {
         fail("Expected a null array, but was: " + Arrays.asList(was));
      }
      else if (was == null)
      {
         fail("Expected " + Arrays.asList(exp) + ", but was: null");
      }
   }

   private static void assertEqualsArray(byte[] exp, byte[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(boolean[] exp, boolean[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(short[] exp, short[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(int[] exp, int[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(long[] exp, long[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(float[] exp, float[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(double[] exp, double[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

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

}