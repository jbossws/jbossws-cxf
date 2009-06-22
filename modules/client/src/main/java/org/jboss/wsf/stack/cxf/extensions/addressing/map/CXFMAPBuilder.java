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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.ws.addressing.AddressingBuilder;
import org.apache.cxf.ws.addressing.AddressingConstants;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPConstants;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.addressing.MAPRelatesTo;

/**
 * MAPBuilder is a helper used to create objects used with class MAP. This is the JBossWS CXF
 * implementation.
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 26-May-2009
 *
 */
public class CXFMAPBuilder implements MAPBuilder
{
   private AddressingBuilder addressingBuilder;

   private static MAPBuilder theBuilder = new CXFMAPBuilder();

   public static MAPBuilder getBuilder()
   {
      return theBuilder;
   }

   private CXFMAPBuilder()
   {
      AddressingBuilder implementation = AddressingBuilder.getAddressingBuilder();
      this.addressingBuilder = implementation;
   }

   public MAP newMap()
   {
      AddressingProperties implementation = addressingBuilder.newAddressingProperties();
      return new CXFMAP(implementation);
   }

   /**
    * retrieve the inbound server message address properties attached to a message context
    * @param ctx the server message context
    * @return
    */
   public MAP inboundMap(Map<String, Object> ctx)
   {
      AddressingProperties implementation = (AddressingProperties)ctx.get(CXFMAPConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      return newMap(implementation);
   }

   /**
    * retrieve the outbound client message address properties attached to a message request map
    * @param ctx the client request properties map
    * @return
    */
   public MAP outboundMap(Map<String, Object> ctx)
   {
      AddressingProperties implementation = (AddressingProperties)ctx.get(CXFMAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      if (implementation == null)
      {
         implementation = addressingBuilder.newAddressingProperties();
         ctx.put(CXFMAPConstants.CLIENT_ADDRESSING_PROPERTIES, implementation);
         ctx.put(CXFMAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, implementation);
      }
      return newMap(implementation);
   }

   // n.b. this is package public only!
   MAP newMap(AddressingProperties implementation)
   {
      return new CXFMAP(implementation);
   }

   public MAPConstants newConstants()
   {
      AddressingConstants implementation = addressingBuilder.newAddressingConstants();
      return new CXFMAPConstants(implementation);
   }

   public MAPEndpoint newEndpoint(String address)
   {
      EndpointReferenceType implementation = new EndpointReferenceType();
      AttributedURIType uri = new AttributedURIType();
      uri.setValue(address);
      implementation.setAddress(uri);
      return new CXFMAPEndpoint(implementation);
   }

   public MAPRelatesTo newRelatesTo(String id, QName type)
   {
      return new CXFMAPRelatesTo(id, type);
   }

}
