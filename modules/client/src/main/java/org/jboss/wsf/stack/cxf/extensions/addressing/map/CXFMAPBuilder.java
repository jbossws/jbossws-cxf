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
package org.jboss.wsf.stack.cxf.extensions.addressing.map;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.ws.addressing.AddressingConstants;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPConstants;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAPRelatesTo;

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
   private static final MAPBuilder theBuilder = new CXFMAPBuilder();

   public static MAPBuilder getBuilder()
   {
      return theBuilder;
   }

   private CXFMAPBuilder()
   {
   }

   public MAP newMap()
   {
      AddressingProperties implementation = new AddressingProperties();
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
         implementation = new AddressingProperties();
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
      AddressingConstants implementation = new AddressingConstants();
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
