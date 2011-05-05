/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.extensions.addressing.map;

import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.ReferenceParametersType;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.w3c.dom.Element;

/**
 * MAPEndpoint is a wrapper which works with class MAP. This is the JBossWS CXF implementation.
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 26-May-2009
 *
 */
public class CXFMAPEndpoint implements MAPEndpoint
{
   private EndpointReferenceType implementation;

   CXFMAPEndpoint(EndpointReferenceType implementation)
   {
      this.implementation = implementation;
   }

   public String getAddress()
   {
      return implementation.getAddress().getValue();
   }

   public void addReferenceParameter(Element element)
   {
      ReferenceParametersType refParams = implementation.getReferenceParameters();
      if (refParams == null)
      {
         refParams = new ReferenceParametersType();
         implementation.setReferenceParameters(refParams);
      }
      refParams.getAny().add(element);
   }

   EndpointReferenceType getImplementation()
   {
      return implementation;
   }

   public List<Object> getReferenceParameters()
   {
      List<Object> list = new LinkedList<Object>();
      ReferenceParametersType refParams = implementation.getReferenceParameters();
      if (refParams != null)
      {
         List<Object> any = refParams.getAny();
         if (any != null)
         {
            list.addAll(any);
         }
      }
      return list;
   }

}
