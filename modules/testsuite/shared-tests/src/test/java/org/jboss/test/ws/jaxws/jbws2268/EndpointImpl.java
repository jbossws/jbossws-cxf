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
package org.jboss.test.ws.jaxws.jbws2268;

import java.io.File;
import java.io.FileWriter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

/**
 * Endpoint Implementation
 * @author richard.opalka@jboss.com
 */
@WebService(
   serviceName = "EndpointService",
   targetNamespace = "http://www.jboss.org/test/ws/jaxws/jbws2268",
   endpointInterface="org.jboss.test.ws.jaxws.jbws2268.EndpointInterface"
)
public class EndpointImpl
{

   private final StringBuffer builder = new StringBuffer();
   private volatile File file;
   
   public EndpointImpl()
   {
      super();
   }
   
   @PostConstruct
   protected void init()
   {
      this.builder.append("init() ");
   }
   
   @WebMethod
   public boolean setFile(String targetFile)
   {
      this.file = new File(targetFile);
      return this.file.exists();
   }
   
   @PreDestroy
   protected void destroy()
   {
      if (file != null) {
         this.builder.append("destroy()");
         this.writeTestLog();
      }
   }
   
   private void writeTestLog()
   {
      try
      {
         FileWriter fw = new FileWriter(this.file);
         fw.write(this.builder.toString());
         fw.close();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

}
