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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.util.ArrayList;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;

// standard EJB3 annotations
@Remote(EJB3Remote.class)
@Stateless

// Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = EndpointService.class, type = EndpointService.class, wsdlLocation = "META-INF/wsdl/Endpoint.wsdl")

// Test multiple on type
@WebServiceRefs
(
   {
      @WebServiceRef(name = "service2", value = EndpointService.class, type = EndpointService.class),
      @WebServiceRef(name = "port1", value = EndpointService.class, type = Endpoint.class)
   }
)
public class EJB3Client implements EJB3Remote
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJB3Client.class);

   // Test on field with name
   @WebServiceRef(name = "EndpointService3")
   public EndpointService service3;

   // Test on field without name
   @WebServiceRef
   public EndpointService service4;

   // Test on method with value
   @WebServiceRef(name = "EndpointService5")
   public void setService5(EndpointService service)
   {
      this.service5 = service;
   }
   private EndpointService service5;
   
   // Test on method without name
   @WebServiceRef
   public void setService6(EndpointService service)
   {
      this.service6 = service;
   }
   private EndpointService service6;
   
   //Test on field without name and no wsdl override in descriptor
   @WebServiceRef
   public EndpointService service7;
   
   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = EndpointService.class)
   public Endpoint port2;

   // Test on field with value
   @WebServiceRef(value = EndpointService.class)
   public Endpoint port3;

   public String echo(String inStr)
   {
      log.info("echo: " + inStr);

      InitialContext initCtx = null;
      String envRoot = "java:comp.ejb3";
      try
      {
         initCtx = new InitialContext();
         initCtx.lookup(envRoot + "/env/service1");
      }
      catch (NamingException ne)
      {
         if (initCtx == null)
            throw new WebServiceException(ne);
         
         // EJBTHREE-1378
         envRoot = "java:comp";
         try 
         {
            initCtx.lookup(envRoot + "/env/service1");
         }
         catch (Exception e)
         {
            throw new WebServiceException(e);
         }
      }
      
      ArrayList<Endpoint> ports = new ArrayList<Endpoint>();
      try
      {
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/service1")).getEndpointPort());
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/service2")).getEndpointPort());
         ports.add((Endpoint)service3.getPort(Endpoint.class));
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/EndpointService3")).getEndpointPort());
         ports.add((Endpoint)service4.getPort(Endpoint.class));
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/" + getClass().getName() + "/service4")).getEndpointPort());
         ports.add((Endpoint)service5.getPort(Endpoint.class));
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/EndpointService5")).getEndpointPort());
         ports.add((Endpoint)service6.getPort(Endpoint.class));
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/" + getClass().getName() + "/service6")).getEndpointPort());
         ports.add((Endpoint)service7.getPort(Endpoint.class));
         ports.add(((EndpointService)initCtx.lookup(envRoot + "/env/" + getClass().getName() + "/service7")).getEndpointPort());
         ports.add((Endpoint)initCtx.lookup(envRoot + "/env/port1"));
         ports.add(port2);
         ports.add((Endpoint)initCtx.lookup(envRoot + "/env/Port2"));
         ports.add(port3);
         ports.add((Endpoint)initCtx.lookup(envRoot + "/env/" + getClass().getName() + "/port3"));
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new WebServiceException(ex);
      }

      for (Endpoint port : ports)
      {
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }

      return inStr;
   }
}
