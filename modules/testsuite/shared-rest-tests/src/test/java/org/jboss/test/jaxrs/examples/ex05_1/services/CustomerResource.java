/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jaxrs.examples.ex05_1.services;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.jboss.test.jaxrs.examples.ex05_1.domain.Customer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/customers")
public class CustomerResource
{
   private Map<Integer, Customer> customerDB = Collections.synchronizedMap(new LinkedHashMap<Integer, Customer>());

   public CustomerResource()
   {
      Customer customer;
      int id = 1;

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Bill");
      customer.setLastName("Burke");
      customer.setStreet("263 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Joe");
      customer.setLastName("Burke");
      customer.setStreet("263 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Monica");
      customer.setLastName("Burke");
      customer.setStreet("263 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);

      customer = new Customer();
      customer.setId(id);
      customer.setFirstName("Steve");
      customer.setLastName("Burke");
      customer.setStreet("263 Clarendon Street");
      customer.setCity("Boston");
      customer.setState("MA");
      customer.setZip("02115");
      customer.setCountry("USA");
      customerDB.put(id++, customer);
   }

   @GET
   @Produces("application/xml")
   public StreamingOutput getCustomers(final @QueryParam("start") int start,
                                       final @QueryParam("size") @DefaultValue("2") int size)
   {
      return new StreamingOutput()
      {
         public void write(OutputStream outputStream) throws IOException, WebApplicationException
         {
            PrintStream writer = new PrintStream(outputStream);
            writer.println("<customers>");
            synchronized (customerDB)
            {
               int i = 0;
               for (Customer customer : customerDB.values())
               {
                  if (i >= start && i < start + size) outputCustomer("   ", writer, customer);
                  i++;
               }
            }
            writer.println("</customers>");
         }
      };
   }

   @GET
   @Produces("application/xml")
   @Path("uriinfo")
   public StreamingOutput getCustomers(@Context UriInfo info)
   {
      int start = 0;
      int size = 2;
      if (info.getQueryParameters().containsKey("start"))
      {
         start = Integer.valueOf(info.getQueryParameters().getFirst("start"));
      }
      if (info.getQueryParameters().containsKey("size"))
      {
         size = Integer.valueOf(info.getQueryParameters().getFirst("size"));
      }
      return getCustomers(start, size);
   }

   protected void outputCustomer(String indent, PrintStream writer, Customer cust) throws IOException
   {
      writer.println(indent + "<customer id=\"" + cust.getId() + "\">");
      writer.println(indent + "   <first-name>" + cust.getFirstName() + "</first-name>");
      writer.println(indent + "   <last-name>" + cust.getLastName() + "</last-name>");
      writer.println(indent + "   <street>" + cust.getStreet() + "</street>");
      writer.println(indent + "   <city>" + cust.getCity() + "</city>");
      writer.println(indent + "   <state>" + cust.getState() + "</state>");
      writer.println(indent + "   <zip>" + cust.getZip() + "</zip>");
      writer.println(indent + "   <country>" + cust.getCountry() + "</country>");
      writer.println(indent + "</customer>");
   }

}