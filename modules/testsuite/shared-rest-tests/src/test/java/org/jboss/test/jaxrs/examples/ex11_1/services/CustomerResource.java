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
package org.jboss.test.jaxrs.examples.ex11_1.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.test.jaxrs.examples.ex11_1.domain.Customer;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/customers")
public class CustomerResource
{
   private Map<Integer, Customer> customerDB = new ConcurrentHashMap<Integer, Customer>();
   private AtomicInteger idCounter = new AtomicInteger();

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
   }

   @POST
   @Consumes("application/xml")
   public Response createCustomer(Customer customer)
   {
      customer.setId(idCounter.incrementAndGet());
      customerDB.put(customer.getId(), customer);
      System.out.println("Created customer " + customer.getId());
      return Response.created(URI.create("/customers/" + customer.getId())).build();

   }

   @GET
   @Path("{id}")
   @Produces("application/xml")
   public Response getCustomer(@PathParam("id") int id,
                               @HeaderParam("If-None-Match") String sent,
                               @Context Request request)
   {
      Customer cust = customerDB.get(id);
      if (cust == null)
      {
         throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      if (sent == null) System.out.println("No If-None-Match sent by client");

      EntityTag tag = new EntityTag(Integer.toString(cust.hashCode()));

      CacheControl cc = new CacheControl();
      cc.setMaxAge(5);


      Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
      if (builder != null)
      {
         System.out.println("** revalidation on the server was successful");
         builder.cacheControl(cc);
         return builder.build();
      }


      // Preconditions not met!

      cust.setLastViewed(new Date().toString());
      builder = Response.ok(cust, "application/xml");
      builder.cacheControl(cc);
      builder.tag(tag);
      return builder.build();
   }


   @Path("{id}")
   @PUT
   @Consumes("application/xml")
   public Response updateCustomer(@PathParam("id") int id,
                                  @Context Request request,
                                  Customer update)
   {
      Customer cust = customerDB.get(id);
      if (cust == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
      EntityTag tag = new EntityTag(Integer.toString(cust.hashCode()));

      Response.ResponseBuilder builder =
              request.evaluatePreconditions(tag);

      if (builder != null)
      {
         // Preconditions not met!
         return builder.build();
      }

      // Preconditions met, perform update

      cust.setFirstName(update.getFirstName());
      cust.setLastName(update.getLastName());
      cust.setStreet(update.getStreet());
      cust.setState(update.getState());
      cust.setZip(update.getZip());
      cust.setCountry(update.getCountry());


      builder = Response.noContent();
      return builder.build();
   }
}