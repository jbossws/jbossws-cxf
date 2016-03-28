/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.jaxrs.integration.cdi.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/root")
public class EJBRootResource
{

   @Context
   private UriInfo uri;

   @GET
   public String get()
   {
      return "GET: " + uri.getRequestUri().toASCIIString() + "From EJB Root Resource";
   }

   @EJB
   EJBResource r;

   @Path("/sub")
   public EJBResource getSub()
   {
      return r;
   }

   @EJB
   EJBLocal rl;

   @Path("/local")
   public EJBLocal getLocal()
   {
      return rl;
   }

   @Path("exception")
   @GET
   public String throwException()
   {
      throw new EJBException(new WebApplicationException(Status.CREATED));
   }

   @Context
   private Application application;

   private boolean isPostConstruct = false;

   @PostConstruct
   public void postConstruct()
   {
      isPostConstruct = application != null;
   }

   @Path("priorpost")
   @GET
   public String jaxrsInjectPriorPostConstructOnRootResource()
   {
      return String.valueOf(isPostConstruct);
   }
}
