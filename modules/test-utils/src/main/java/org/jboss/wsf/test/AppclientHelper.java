/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.ws.common.concurrent.CopyJob;
import org.jboss.ws.common.io.TeeOutputStream;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class AppclientHelper
{

   private static final String JBOSS_HOME = System.getProperty("jboss.home");
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final int TIMEOUT = Integer.getInteger("appclient.timeout", 120);
   private static final String EXT = ":".equals(PS) ? ".sh" : ".bat";
   private static final String appclientScript = JBOSS_HOME + FS + "bin" + FS + "appclient" + EXT;
   private static final Semaphore s = new Semaphore(1, true); //one appclient only can be running at the same time ATM
   private static Map<String, AppclientProcess> appclients = Collections.synchronizedMap(new HashMap<String, AppclientProcess>(2));
   private static ExecutorService executors = Executors.newCachedThreadPool(AppclientDaemonFactory.INSTANCE);
   private static String appclientOutputDir;

   private static class AppclientProcess {
      public Process process;
      public CopyJob outTask;
      public CopyJob errTask;
      public OutputStream output;
      public OutputStream log;
   }

   private AppclientHelper()
   {
      // forbidden instantiation
   }

   /**
    * Triggers appclient deployment and returns the corresponding Process
    * Please note the provided output stream (if any) is not automatically closed.
    *
    * @param archive
    * @param appclientOS
    * @param appclientArgs
    * @return
    * @throws Exception
    */
   static Process deployAppclient(final String archive, final OutputStream appclientOS, final String... appclientArgs) throws Exception
   {
      final AppclientProcess ap = newAppclientProcess(archive, appclientOS, appclientArgs);
      final String patternToMatch = "Deployed \"" + getAppclientEarName(archive) + "\"";
      if (!awaitOutput(ap.output, patternToMatch)) {
         throw new RuntimeException("Cannot deploy " + getAppclientFullName(archive) + " to appclient");
      }
      appclients.put(archive, ap);
      return ap.process;
   }

   static void undeployAppclient(final String archive, boolean awaitShutdown) throws Exception
   {
      final AppclientProcess ap = appclients.remove(archive);
      try
      {
         if (awaitShutdown)
         {
            shutdownAppclient(archive, ap.output);
         }
      }
      finally
      {
         s.release();
         //NPE checks to avoid hiding other exceptional conditions that led to premature undeploy..
         if (ap != null) {
            if (ap.output != null) {
               ap.outTask.kill();
            }
            if (ap.errTask != null) {
               ap.errTask.kill();
            }
            if (ap.process != null) {
               ap.process.destroy();
            }
            if (ap.log != null) {
               ap.log.close();
            }
         }
      }
   }

   private static AppclientProcess newAppclientProcess(final String archive, final OutputStream appclientOS, final String... appclientArgs) throws Exception
   {
      s.acquire();
      try {
         final String killFileName = getKillFileName(archive);
         final String appclientFullName = getAppclientFullName(archive);
         final String appclientShortName = getAppclientShortName(archive);
         final AppclientProcess ap = new AppclientProcess();
         ap.output = new ByteArrayOutputStream();
         final List<String> args = new LinkedList<String>();
         args.add(appclientScript);
         String appclientConfigName = System.getProperty("APPCLIENT_CONFIG_NAME", "appclient.xml");
         String configArg = "--appclient-config=" + appclientConfigName;
         args.add(configArg);
         args.add(appclientFullName);
         if (appclientOS == null)
         {
            args.add(killFileName);
         }
         else
         {
            // propagate appclient args
            for (final String appclientArg : appclientArgs)
            {
               args.add(appclientArg);
            }
         }

         //note on output streams closing: we're not caring about closing any here as it's quite a complex thing due to the TeeOutputStream nesting;
         //we're however still safe, given the ap.output is a ByteArrayOutputStream (whose .close() does nothing), ap.log is explicitly closed at
         //undeploy and closing appclientOS is a caller responsibility.

         ap.log = new FileOutputStream(new File(getAppclientOutputDir(), appclientShortName + ".log-" + System.currentTimeMillis()));
         @SuppressWarnings("resource")
         final OutputStream logOutputStreams = (appclientOS == null) ? ap.log : new TeeOutputStream(ap.log, appclientOS);
         printLogTrailer(logOutputStreams, appclientFullName);

         final ProcessBuilder pb = new ProcessBuilder().command(args);
         // always propagate IPv6 related properties
         final StringBuilder javaOptsValue = new StringBuilder();

         // wildfly9 security manage flag changed from -Djava.security.manager to -secmgr.
         // Can't pass -secmgr arg through arquillian because it breaks arquillian's
         // config of our tests.
         // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
         // come after the jboss-modules.jar ref.
         String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
         if (additionalJVMArgs != null) {

            if ("-Djava.security.manager".equals(additionalJVMArgs)) {
               System.setProperty("SECMGR", "true");
               javaOptsValue.append("-Djava.security.policy="
                + System.getProperty("securityPolicyfile", "")).append(" ");
            }
         } else {
            javaOptsValue.append("-Djava.net.preferIPv4Stack=").append(System.getProperty("java.net.preferIPv4Stack", "true")).append(" ");
            javaOptsValue.append("-Djava.net.preferIPv6Addresses=").append(System.getProperty("java.net.preferIPv6Addresses", "false")).append(" ");
         }
         javaOptsValue.append("-Djboss.bind.address=").append(undoIPv6Brackets(System.getProperty("jboss.bind.address", "localhost"))).append(" ");
         String appclientDebugOpts = System.getProperty("APPCLIENT_DEBUG_OPTS", null);
         if (appclientDebugOpts != null && appclientDebugOpts.trim().length() > 0)
            javaOptsValue.append(appclientDebugOpts).append(" ");
         pb.environment().put("JAVA_OPTS", javaOptsValue.toString());
         System.out.println("JAVA_OPTS=\"" + javaOptsValue.toString() + "\"");
         System.out.println("Starting " + appclientScript + " " + configArg + " "
             + appclientFullName + (appclientArgs == null ? "" :  " with args "
             + Arrays.asList(appclientArgs)));
         ap.process = pb.start();
         // appclient out
         ap.outTask = new CopyJob(ap.process.getInputStream(), new TeeOutputStream(ap.output, logOutputStreams));
         // appclient err
         ap.errTask = new CopyJob(ap.process.getErrorStream(), ap.log);
         // unfortunately the following threads are needed because of Windows behavior
         executors.submit(ap.outTask);
         executors.submit(ap.errTask);
         return ap;
      } catch (Exception e) {
         s.release();
         throw e;
      }
   }

   private static void printLogTrailer(OutputStream logOutputStreams, String appclientFullName) {
      final PrintWriter pw = new PrintWriter(new OutputStreamWriter(logOutputStreams, StandardCharsets.UTF_8));
      pw.write("Starting appclient process: " + appclientFullName + "...\n");
      pw.flush();
   }

   private static String undoIPv6Brackets(final String s)
   {
      return s.startsWith("[") ? s.substring(1, s.length() - 1) : s;
   }

   private static void shutdownAppclient(final String archive, final OutputStream os) throws IOException, InterruptedException
   {
      final File killFile = new File(getKillFileName(archive));
      killFile.createNewFile();
      try
      {
         if (!awaitOutput(os, "stopped in")) {
            throw new RuntimeException("Cannot undeploy " + getAppclientFullName(archive) + " from appclient");
         }
      }
      finally
      {
         if (!killFile.delete())
         {
            killFile.deleteOnExit();
         }
      }
   }

   private static boolean awaitOutput(final OutputStream os, final String patternToMatch) throws InterruptedException {
      int countOfAttempts = 0;
      final int maxCountOfAttempts = TIMEOUT * 2; // max wait time: default 2 minutes
      while (!os.toString().contains(patternToMatch))
      {
         Thread.sleep(500);
         if (countOfAttempts++ == maxCountOfAttempts)
         {
            return false;
         }
      }
      return true;
   }

   private static String getKillFileName(final String archive)
   {
      final int sharpIndex = archive.indexOf('#');
      return JBOSS_HOME + FS + "bin" + FS + archive.substring(sharpIndex + 1) + ".kill";
   }

   private static String getAppclientOutputDir()
   {
      if (appclientOutputDir == null)
      {
         appclientOutputDir = System.getProperty("appclient.output.dir");
         if (appclientOutputDir == null)
         {
            throw new IllegalStateException("System property appclient.output.dir not configured");
         }
         final File appclientOutputDirectory = new File(appclientOutputDir);
         if (!appclientOutputDirectory.exists())
         {
            if (!appclientOutputDirectory.mkdirs())
            {
               throw new IllegalStateException("Unable to create directory " + appclientOutputDir);
            }
         }
      }
      return appclientOutputDir;
   }

   private static String getAppclientFullName(final String archive)
   {
      final int sharpIndex = archive.indexOf('#');
      final String earName = archive.substring(0, sharpIndex);
      return JBossWSTestHelper.getArchiveFile(earName).getParent() + FS + archive;
   }

   private static String getAppclientShortName(final String archive)
   {
      final int sharpIndex = archive.indexOf('#');
      return archive.substring(sharpIndex + 1);
   }

   private static String getAppclientEarName(final String archive)
   {
      final int sharpIndex = archive.indexOf('#');
      return archive.substring(0, sharpIndex);
   }

   // [JBPAPP-10027] appclient threads are always daemons (to don't block JVM shutdown)
   private static class AppclientDaemonFactory implements ThreadFactory {
       static final AppclientDaemonFactory INSTANCE = new AppclientDaemonFactory();
       final ThreadGroup group;
       final AtomicInteger threadNumber = new AtomicInteger(1);
       final String namePrefix;

       AppclientDaemonFactory() {
           group = Thread.currentThread().getThreadGroup();
           namePrefix = "appclient-output-processing-daemon-";
       }

       public Thread newThread(final Runnable r) {
           final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
           t.setDaemon(true);
           t.setPriority(Thread.NORM_PRIORITY);
           return t;
       }
   }

}
