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
package org.jboss.test.ws.jaxws.benchmark;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.ws.common.DOMUtils;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Sep-2009
 *
 */
public class Runner
{

   private static int threadCount = 5;

   private static int runs = 10;

   private static int iterations = 10;

   private static long sleep = 1500;

   private static boolean verbose = false;

   private static String address = "localhost:8080";

   public static class BenchmarkCallable implements Callable<Result>
   {

      private final BenchmarkTest test;

      private int iterations;

      private String id;

      public BenchmarkCallable(String id, BenchmarkTest test, int iterations)
      {
         this.test = test;
         this.iterations = iterations;
         this.id = id;
      }

      public Result call() throws Exception
      {
         Result result = new Result(id);
         long startTime = new Date().getTime();
         Object port = test.prepare();
         result.setPreparationTime(new Date().getTime() - startTime);
         startTime = new Date().getTime();
         for (int i = 0; i < iterations; i++)
         {
            try
            {
               test.performIteration(port);
               result.success();
            }
            catch (Throwable e)
            {
               e.printStackTrace();
               result.failure();
            }
         }
         result.setProcessingTime(new Date().getTime() - startTime);
         return result;
      }
   }

   private static BenchmarkTest parseArguments(String[] args) throws Exception
   {
      String shortOpts = "t:r:s:i:a:vh";
      LongOpt[] longOpts =
      {new LongOpt("deployment", LongOpt.REQUIRED_ARGUMENT, null, 'd'),
            new LongOpt("threads", LongOpt.REQUIRED_ARGUMENT, null, 't'),
            new LongOpt("runs", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
            new LongOpt("sleep", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("iterations", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
            new LongOpt("address", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
            new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'), new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')};

      Getopt getopt = new Getopt("Benchmark-runner", args, shortOpts, longOpts);
      int c;
      while ((c = getopt.getopt()) != -1)
      {
         switch (c)
         {
            case 't' :
               threadCount = Integer.parseInt(getopt.getOptarg());
               break;
            case 'r' :
               runs = Integer.parseInt(getopt.getOptarg());
               break;
            case 's' :
               sleep = Long.parseLong(getopt.getOptarg());
               break;
            case 'i' :
               iterations = Integer.parseInt(getopt.getOptarg());
               break;
            case 'a' :
               address = getopt.getOptarg();
               break;
            case 'v' :
               verbose = true;
               break;
            case 'h' :
               printHelp();
               System.exit(0);
            case '?' :
               System.exit(1);
         }
      }

      int classPos = getopt.getOptind();
      if (classPos >= args.length)
      {
         System.err.println("Error: test-class was not specified!");
         printHelp();
         System.exit(1);
      }

      try
      {
         Class<?> clazz = Class.forName(args[classPos]);
         System.out.println(BenchmarkTest.class.isAssignableFrom(clazz));
         System.out.println(clazz.isAssignableFrom(BenchmarkTest.class));

         return (BenchmarkTest) clazz.getDeclaredConstructor().newInstance();
      }
      catch (Exception e)
      {
         System.out.println("Cannot instanciate " + args[classPos]);
         throw e;
      }
   }

   private static void printHelp()
   {
      PrintStream out = System.out;
      out.println("Benchmark-runner is a cmd line tool that performs a benchmark running a test multiple time\n");
      out.println("usage: Benchmark-runner [options] <test-class>\n");
      out.println("options: ");
      out.println("    -h, --help                        Show this help message");
      out.println("    -v, --verbose                     Show verbose results");
      out.println("    -s, --sleep=<time-in-ms>          How many millisecs to wait between runs");
      out.println("    -a, --address=<host:port>         Server host:port");
      out.println("    -r, --runs=<number-of-runs>       How many runs to do");
      out.println("    -i, --iterations=<num-of-iters>   How many iterations to do in each run");
      out.println("    -t, --threads=<number-of-threads> How many concurrent threads to create");
      out.flush();
   }

   /**
    * @param args
    */
   public static void main(String[] args) throws Exception
   {
      NumberFormat speedForm = new DecimalFormat("0.00");

      BenchmarkTest test = parseArguments(args);

      // Workaround for JBWS-2681
      test.prepare();

      List<Callable<Result>> callables = new ArrayList<Callable<Result>>(threadCount);

      long total = 0;
      long failed = 0;
      long procTime = 0;
      ExecutorService es = Executors.newFixedThreadPool(threadCount);
      for (int run = 0; run < runs; run++)
      {
         System.out.println("*\n RUN " + run + "\n*");
         callables.clear();
         for (int i = 0; i < threadCount; i++)
         {
            callables.add(new BenchmarkCallable(String.valueOf(i), test, iterations));
         }

         DOMUtils.clearThreadLocals();

         long startProcTime = new Date().getTime();
         List<Future<Result>> futures = es.invokeAll(callables);
         long currentRunProcTime = new Date().getTime() - startProcTime;
         procTime = procTime + currentRunProcTime;

         try
         {
            Thread.sleep(sleep);
         }
         catch (InterruptedException ie)
         {
            //ignore
         }

         long currentRunIterations = 0;
         for (Future<Result> f : futures)
         {
            Result result = f.get();
            long subtotal = result.getTotal();
            currentRunIterations = currentRunIterations + subtotal;
            total = total + subtotal;
            failed = failed + result.getFailures();
            if (verbose)
            {
               System.out.println(result.getId() + " -> " + result.getAverageProcessingTime() + " ms/iteration, "
                     + subtotal + " iterations, " + result.getFailures() + " failures, " + result.getPreparationTime()
                     + " ms of preparation.");
            }
         }

         System.out.println();
         System.out.println("----------- RUN STATS -------------");
         System.out.println("Iterations: " + currentRunIterations);
         System.out.println("Total processing time: " + currentRunProcTime + " ms");
         System.out.println("Iterations/sec: " + speedForm.format(1000f * currentRunIterations / currentRunProcTime));
         System.out.println("------------------------------------");
         System.out.println();
      }
      es.shutdown();
      
      System.out.println("------------------------------------");
      System.out.println("Iterations: " + total);
      System.out.println("Failures: " + failed);
      System.out.println("Total processing time: " + procTime + " ms");
      System.out.println("Iterations/sec: " + speedForm.format(1000f * total / procTime));
      System.out.println("------------------------------------");

   }

   public static String getServerAddress()
   {
      return address;
   }

}
