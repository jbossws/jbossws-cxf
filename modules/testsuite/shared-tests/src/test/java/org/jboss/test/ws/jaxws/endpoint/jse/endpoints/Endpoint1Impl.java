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
package org.jboss.test.ws.jaxws.endpoint.jse.endpoints;

import java.io.IOException;

import jakarta.activation.DataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.MTOM;

/**
 * Service implementation.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService
(
   serviceName = "Endpoint1Impl",
   targetNamespace = "http://org.jboss.ws/jaxws/endpoint/jse/endpoints/",
   endpointInterface = "org.jboss.test.ws.jaxws.endpoint.jse.endpoints.Endpoint1Iface"
)
@MTOM
public class Endpoint1Impl implements Endpoint1Iface
{

   private int count;
   private boolean initialized;

   public String echo(String input)
   {
      count++;
      return input;
   }
   
   @PostConstruct
   public void init()
   {
      this.initialized = true;
   }

   @PreDestroy
   public void destroy()
   {
      //nothing to do
   }

   public int getCount()
   {
      this.ensureInit();
      return count;
   }
   
   public void getException()
   {
      this.ensureInit();
      throw new WebServiceException("Ooops");
   }

   public DHResponse echoDataHandler(DHRequest request)
   {
      this.ensureInit();
      DataHandler dataHandler = request.getDataHandler();

      try
      {
         if (!dataHandler.getContentType().equals("text/plain"))
         {
            throw new WebServiceException("Wrong content type");
         }
         if (!dataHandler.getContent().equals("some string"))
         {
            throw new WebServiceException("Wrong data");
         }
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
      
      DataHandler responseData = new DataHandler("Server data", "text/plain");
      return new DHResponse(responseData);
   }
   
   private void ensureInit()
   {
      if (!this.initialized)
         throw new IllegalStateException();
   }

}
