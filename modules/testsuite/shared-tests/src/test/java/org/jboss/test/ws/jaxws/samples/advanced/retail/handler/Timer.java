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
package org.jboss.test.ws.jaxws.samples.advanced.retail.handler;

import java.util.Stack;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 9, 2006
 */
public class Timer
{
   private Stack<Long> timings = new Stack<Long>();

   private static Timer ourInstance = new Timer();

   public static Timer getInstance()
   {
      return ourInstance;
   }

   private Timer() {}

   public synchronized void push(long t)
   {
      timings.push(Long.valueOf(t));
   }

   public synchronized long pop()
   {
      if(timings.isEmpty())
         return 0;

      Long aLong = timings.pop();
      return System.currentTimeMillis() - aLong.longValue();
   }
}
