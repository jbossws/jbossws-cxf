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

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Sep-2009
 *
 */
public class Result
{
   private String id;
   private int failures = 0;
   private int successes = 0;
   private long processingTime;
   private long preparationTime;
   
   public Result(String id)
   {
      this.id = id;
   }
   
   public void failure()
   {
      failures++;
   }
   
   public void success()
   {
      successes++;
   }

   public int getFailures()
   {
      return failures;
   }

   public int getSuccesses()
   {
      return successes;
   }

   public String getId()
   {
      return id;
   }

   public long getProcessingTime()
   {
      return processingTime;
   }

   public void setProcessingTime(long processingTime)
   {
      this.processingTime = processingTime;
   }
   
   public int getTotal()
   {
      return successes + failures;
   }
   
   public double getAverageProcessingTime()
   {
      return Math.round(100 * processingTime / getTotal()) / 100;
   }

   public long getPreparationTime()
   {
      return preparationTime;
   }

   public void setPreparationTime(long preparationTime)
   {
      this.preparationTime = preparationTime;
   }
}
