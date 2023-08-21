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
package org.jboss.test.ws.jaxws.jbws3556;

public class MyException extends Exception {

   private static final long serialVersionUID = -1349782382698409653L;
   private String from;
   private int id;
   private String summary;

   public MyException() {} // mandatory constructor

   public MyException(String from, int id, String message, String summary) {
      super(message);
      this.from = from;
      this.id = id;
      this.summary = summary;
   }

   // mandatory from setter
   public void setFrom(String from) {
      this.from = from;
   }

    // mandatory id setter
   public void setId(int id) {
      this.id = id;
   }

   // mandatory summary setter
   public void setSummary(String summary) {
      this.summary = summary;
   }

   public String getFrom() {
      return from;
   }

   public int getId() {
      return id;
   }

   public String getSummary() {
      return summary;
   }

   @Override
   public String toString() {
      return from + "," + id + "," + getMessage() + "," + summary;
   }
}