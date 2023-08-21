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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import jakarta.activation.DataSource;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GeneratorDataSource implements DataSource
{
   private long size;

   public GeneratorDataSource(long size)
   {
      this.size = size;
   }

   public String getContentType()
   {
      return "application/octet-stream";
   }

   public InputStream getInputStream() throws IOException
   {
      return new FakeInputStream(size);
   }

   public String getName()
   {
      return null;
   }

   public OutputStream getOutputStream() throws IOException
   {
      return null;
   }
}
