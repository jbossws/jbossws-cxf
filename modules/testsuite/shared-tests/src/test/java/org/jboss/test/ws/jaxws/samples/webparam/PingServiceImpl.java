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
package org.jboss.test.ws.jaxws.samples.webparam;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.Holder;

import org.jboss.logging.Logger;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebParam
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@WebService(name = "PingService", targetNamespace = "http://www.openuri.org/jsr181/WebParamExample")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PingServiceImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(PingServiceImpl.class);

   @WebMethod
   public PingDocument echo(PingDocument p)
   {
      log.info("echo: " + p);
      return p;
   }

   @Oneway
   @WebMethod(operationName = "PingOneWay")
   public void ping(@WebParam(name = "Ping") PingDocument p)
   {
      log.info("ping: " + p);
   }

   @WebMethod(operationName = "PingTwoWay")
   public void ping(@WebParam(name = "Ping", mode = WebParam.Mode.INOUT) Holder<PingDocument> p)
   {
      log.info("ping: " + p.value);
      PingDocument resDoc = new PingDocument();
      resDoc.setContent(p.value.getContent() + " Response");
      p.value = resDoc;
   }

   @Oneway
   @WebMethod(operationName = "SecurePing")
   public void ping(@WebParam(name = "Ping") PingDocument p, @WebParam(name = "SecHeader", header = true) SecurityHeader secHdr)
   {
      log.info("ping: " + p + "," + secHdr);
   }
}
