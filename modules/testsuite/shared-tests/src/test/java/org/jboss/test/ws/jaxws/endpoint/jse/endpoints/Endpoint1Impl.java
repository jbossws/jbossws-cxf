/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.endpoint.jse.endpoints;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

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
