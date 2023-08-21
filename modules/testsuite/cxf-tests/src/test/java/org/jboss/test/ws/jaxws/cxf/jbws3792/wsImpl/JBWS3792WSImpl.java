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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;

import java.util.logging.Logger;

@jakarta.jws.WebService(
   serviceName = "JBWS3792WSService",
   portName = "JBWS3792WSPort",
   targetNamespace = "http://test.jbws3792/",
   wsdlLocation = "http://bar:8080/jbws3792-external-wsdl/jbws3792.wsdl", //this is overridden in webservices.xml, to allow using a property that's resolved at build time
   endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.JBWS3792WS")

public class JBWS3792WSImpl implements JBWS3792WS {

   private static final Logger LOG = Logger.getLogger(JBWS3792WSImpl.class.getName());

   public java.lang.String hello() {
      LOG.info("Executing operation hello");
      try {
         java.lang.String _return = "Hello world!";
         return _return;
      } catch (java.lang.Exception ex) {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

}

