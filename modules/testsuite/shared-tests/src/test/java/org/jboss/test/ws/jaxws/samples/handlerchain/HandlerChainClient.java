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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jws.HandlerChain;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.HandlerResolver;
import jakarta.xml.ws.handler.PortInfo;

import org.jboss.logging.Logger;

public class HandlerChainClient
{
   // provide logging
   private static final Logger log = Logger.getLogger(HandlerChainClient.class);

   @WebServiceRef(name = "Service1")
   @HandlerChain(file = "jaxws-handlers-client.xml")
   static Service service1;

   // Service2 should have no client side handler chain
   @WebServiceRef(name = "Service2")
   static Service service2;

   @WebServiceRef(name = "Service3")
   static Service service3;

   public static Map<String, String> testResult = new HashMap<String, String>();

   public static void main(String[] args) throws Exception
   {
      String testName = args[0];
      String reqStr = args[1];

      HandlerChainClient client = new HandlerChainClient();
      Method method = HandlerChainClient.class.getMethod(testName, new Class[] { String.class });
      try
      {
         String retStr = (String)method.invoke(client, reqStr);
         testResult.put(testName, retStr);
      }
      catch (InvocationTargetException ex)
      {
         log.error("Invocation error", ex);
         testResult.put(testName, ex.getTargetException().toString());
      }
      catch (Exception ex)
      {
         log.error("Error", ex);
         testResult.put(testName, ex.toString());
      }
   }

   public String testService1(String reqStr) throws Exception
   {
      PortInfo info = new PortInfo()
      {
         @Override
         public String getBindingID()
         {
            return "http://schemas.xmlsoap.org/wsdl/soap/http";
         }

         @Override
         public QName getPortName()
         {
            return null;
         }

         @Override
         public QName getServiceName()
         {
            return null;
         }
      };

      HandlerResolver resolver = service1.getHandlerResolver();
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = resolver.getHandlerChain(info);
      if("[LogHandler, AuthorizationHandler, RoutingHandler, MimeHandler]".equals(handlerChain.toString()) == false)
         throw new IllegalStateException("Unexpected resolver handlers: " + handlerChain);

      Endpoint port = service1.getPort(Endpoint.class);
      return port.echo(reqStr);
   }

   public String testService2(String reqStr) throws Exception
   {
      Endpoint port = service2.getPort(Endpoint.class);
      return port.echo(reqStr);
   }

   public String testService3(String reqStr) throws Exception
   {
      Endpoint port = service3.getPort(Endpoint.class);
      return port.echo(reqStr);
   }
}
