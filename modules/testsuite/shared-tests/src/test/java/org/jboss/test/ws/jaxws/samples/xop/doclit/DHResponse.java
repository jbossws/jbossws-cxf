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

import jakarta.activation.DataHandler;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name="dataResponse", namespace = "http://org.jboss.ws/xop/doclit")
public class DHResponse {


   private DataHandler dataHandler;


   public DHResponse() {
   }

   public DHResponse(DataHandler dataHandler) {
      this.dataHandler = dataHandler;
   }

   @XmlMimeType("text/plain")
   public DataHandler getDataHandler() {
      return dataHandler;
   }

   public void setDataHandler(DataHandler dataHandler) {
      this.dataHandler = dataHandler;
   }
}
