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
package org.jboss.test.ws.jaxws.samples.addressing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.Addressing;

import org.jboss.logging.Logger;

/**
 * WS-Addressing stateful service endpoint
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
@WebService(name = "StatefulEndpoint", targetNamespace = "http://org.jboss.ws/samples/wsaddressing", serviceName = "TestService")
@Addressing
@HandlerChain(file = "jaxws-handlers.xml")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class StatefulEndpointImpl implements StatefulEndpoint
{
   // provide logging
   private static Logger log = Logger.getLogger(StatefulEndpointImpl.class);

   public static final QName IDQN = new QName("http://somens", "clientid", "ns1");

   // The state map for all clients
   private static Map<String, List<String>> clientStateMap = new HashMap<String, List<String>>();

   private String clientid;
   private static List<String> items;
   private WebServiceContext context;

   @Resource
   private void setContext(WebServiceContext context)
   {
      log.info("injecting: " + context);
      this.context = context;
   }

   @WebMethod
   public void addItem(String item)
   {
      initSessionState();
      log.info("addItem [clientid=" + clientid + "]: " + item);
      items.add(item);
   }

   @WebMethod
   public void checkout()
   {
      initSessionState();
      log.info("checkout [clientid=" + clientid + "]");
      clientStateMap.remove(clientid);
   }

   @WebMethod
   public String getItems()
   {
      initSessionState();
      log.info("getItems [clientid=" + clientid + "]: " + items);
      return items.toString();
   }

   private void initSessionState()
   {
      MessageContext msgContext = context.getMessageContext();
      clientid = (String)msgContext.get("clientid");
      if (clientid == null)
         throw new IllegalStateException("Cannot obtain clientid");

      // Get the client's items
      items = clientStateMap.get(clientid);
      if (items == null)
      {
         items = new ArrayList<String>();
         clientStateMap.put(clientid, items);
      }
   }
}
