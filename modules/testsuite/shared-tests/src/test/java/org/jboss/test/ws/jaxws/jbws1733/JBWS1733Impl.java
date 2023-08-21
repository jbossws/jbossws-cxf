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
package org.jboss.test.ws.jaxws.jbws1733;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;

@WebService(serviceName = "JBWS1733Service", endpointInterface = "org.jboss.test.ws.jaxws.jbws1733.JBWS1733")
public class JBWS1733Impl implements JBWS1733
{

   @Resource
   private WebServiceContext wsContext;

   public int getCounter()
   {
      MessageContext mc = wsContext.getMessageContext();
      HttpSession session = ((HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST)).getSession();
      
      // Get a session property "counter" from context
      if (session == null)
         throw new WebServiceException("No session in WebServiceContext");
      
      Integer counter = (Integer)session.getAttribute("counter");
      if (counter == null)
      {
         counter = Integer.valueOf(0);
         System.out.println("Starting the Session");
      }
      counter = Integer.valueOf(counter.intValue() + 1);
      session.setAttribute("counter", counter);
      return counter;
   }

}
