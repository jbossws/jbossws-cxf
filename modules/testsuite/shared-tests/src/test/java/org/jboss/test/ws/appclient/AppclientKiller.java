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

package org.jboss.test.ws.appclient;

import java.io.File;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class AppclientKiller
{

   private final static int MAX_COUNT_OF_ATTEMTS = 600;

   public static void main(final String[] args) throws Exception {
      if (args.length != 1) {
         throw new RuntimeException("shutdown mark file name missing");
      }
      final File shutdownMarkFile = new File(args[0]);
      int countOfAttempts = 0;
      while (!shutdownMarkFile.exists()) {
         countOfAttempts++;
         Thread.sleep(100);
         if (countOfAttempts == MAX_COUNT_OF_ATTEMTS) {
            System.out.println("appclient timeout");
            break;
         }
      }
      System.out.println("forcibly stopped in AppclientKiller");
      System.exit(1);
   }

}
