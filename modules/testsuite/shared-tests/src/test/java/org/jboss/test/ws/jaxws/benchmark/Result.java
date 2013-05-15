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
