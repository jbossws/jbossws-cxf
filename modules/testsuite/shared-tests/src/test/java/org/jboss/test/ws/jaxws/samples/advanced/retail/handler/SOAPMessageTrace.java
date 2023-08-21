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

import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

public class SOAPMessageTrace extends GenericSOAPHandler<SOAPMessageContext>
{
   private static final Logger log = Logger.getLogger(SOAPMessageTrace.class);

   private final Timer timer = Timer.getInstance();

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      timer.push(System.currentTimeMillis());
      return true;
   }

   @Override
   public boolean handleOutbound(SOAPMessageContext msgContext)
   {
      log.info("Exectime time: " + timer.pop() + " ms");
      return true;
   }
}
