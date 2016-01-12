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

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 */
@Path("/cars")
public class CarResource
{
   public static enum Color
   {
      red,
      white,
      blue,
      black
   }

   @GET
   @Path("/matrix/{make}/{model}/{year}")
   @Produces("text/plain")
   public String getFromMatrixParam(@PathParam("make") String make,
                                    @PathParam("model") PathSegment car,
                                    @MatrixParam("color") Color color,
                                    @PathParam("year") String year)
   {
      return "A " + color + " " + year + " " + make + " " + car.getPath();
   }


   @GET
   @Path("/segment/{make}/{model}/{year}")
   @Produces("text/plain")
   public String getFromPathSegment(@PathParam("make") String make,
                                    @PathParam("model") PathSegment car,
                                    @PathParam("year") String year)
   {
      String carColor = car.getMatrixParameters().getFirst("color");
      return "A " + carColor + " " + year + " " + make + " " + car.getPath();
   }

   @GET
   @Path("/segments/{make}/{model : .+}/year/{year}")
   @Produces("text/plain")
   public String getFromMultipleSegments(@PathParam("make") String make,
                                         @PathParam("model") List<PathSegment> car,
                                         @PathParam("year") String year)
   {
      String output = "A " + year + " " + make;
      for (PathSegment segment : car)
      {
         output += " " + segment.getPath();
      }
      return output;
   }

   @GET
   @Path("/uriinfo/{make}/{model}/{year}")
   @Produces("text/plain")
   public String getFromUriInfo(@Context UriInfo info)
   {
      String make = info.getPathParameters().getFirst("make");
      String year = info.getPathParameters().getFirst("year");
      PathSegment model = info.getPathSegments().get(3);
      String color = model.getMatrixParameters().getFirst("color");

      return "A " + color + " " + year + " " + make + " " + model.getPath();
   }
}