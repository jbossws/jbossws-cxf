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

import static org.jboss.wsf.stack.cxf.Messages.MESSAGES;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.ReferenceParametersType;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAPRelatesTo;
import org.w3c.dom.Element;

/**
 * Message Addressing Properties is a wrapper for the stack-specific addressing properties
 * classes implemented by JBossWS Native and CXF. It is used to localize dependence upon the WS
 * stack. This is the JBossWS CXF specific implementation.
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 26-May-2009
 *
 */
public class CXFMAP implements MAP
{
   /**
    * the wrapped instance which this class delegates to
    */
   private AddressingProperties implementation;

   CXFMAP(AddressingProperties implementation)
   {
      this.implementation = implementation;
   }

   public String getTo()
   {
      AttributedURIType to = implementation.getTo();
      return (to != null ? to.getValue() : null);
   }

   public MAPEndpoint getFrom()
   {
      EndpointReferenceType from = implementation.getFrom();
      return (from != null ? new CXFMAPEndpoint(from) : null);
   }

   public String getMessageID()
   {
      AttributedURIType messageId = implementation.getMessageID();
      return (messageId != null ? messageId.getValue() : null);
   }

   public String getAction()
   {
      AttributedURIType action = implementation.getAction();
      return (action != null ? action.getValue() : null);
   }

   public MAPEndpoint getFaultTo()
   {
      EndpointReferenceType faultTo = implementation.getFaultTo();
      return (faultTo != null ? new CXFMAPEndpoint(faultTo) : null);
   }

   public MAPEndpoint getReplyTo()
   {
      EndpointReferenceType replyTo = implementation.getReplyTo();
      return (replyTo != null ? new CXFMAPEndpoint(replyTo) : null);
   }

   public MAPRelatesTo getRelatesTo()
   {
      MAPBuilder builder = CXFMAPBuilder.getBuilder();
      RelatesToType relatesTo = implementation.getRelatesTo();
      if (relatesTo != null)
      {
         String type = relatesTo.getRelationshipType();
         QName relatesToType;
         int index = type.indexOf("}");
         if (index == -1)
         {
            relatesToType = new QName(type);
         }
         else
         {
            String ns = type.substring(1, index + 1);
            String name = type.substring(index + 1);
            relatesToType = new QName(ns, name);
         }
         return builder.newRelatesTo(relatesTo.getValue(), relatesToType);
      }
      else
      {
         return null;
      }
   }

   public void setTo(String address)
   {
      if (address != null)
      {
         EndpointReferenceType epref = new EndpointReferenceType();
         AttributedURIType uri = new AttributedURIType();
         uri.setValue(address);
         epref.setAddress(uri);
         implementation.setTo(epref);
      }
      else
      {
         implementation.setTo((EndpointReferenceType)null);
      }
   }

   public void setFrom(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof CXFMAPEndpoint)
         {
            implementation.setFrom(((CXFMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw MESSAGES.unsupportedMapEndpoin(epref);
         }
      }
      else
      {
         implementation.setFrom(null);
      }
   }

   public void setMessageID(String messageID)
   {
      if (messageID != null)
      {
         AttributedURIType uri = new AttributedURIType();
         uri.setValue(messageID);
         implementation.setMessageID(uri);
      }
      else
      {
         implementation.setMessageID(null);
      }
   }

   public void setAction(String action)
   {
      if (action != null)
      {
         AttributedURIType uri = new AttributedURIType();
         uri.setValue(action);
         implementation.setAction(uri);
      }
      else
      {
         implementation.setAction(null);
      }
   }

   public void setReplyTo(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof CXFMAPEndpoint)
         {
            implementation.setReplyTo(((CXFMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw MESSAGES.unsupportedMapEndpoin(epref);
         }
      }
      else
      {
         implementation.setReplyTo(null);
      }
   }

   public void setFaultTo(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof CXFMAPEndpoint)
         {
            implementation.setFaultTo(((CXFMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw MESSAGES.unsupportedMapEndpoin(epref);
         }
      }
      else
      {
         implementation.setFaultTo(null);
      }
   }

   public void setRelatesTo(MAPRelatesTo relatesTo)
   {
      if (relatesTo != null)
      {
         RelatesToType relatesToImpl = new RelatesToType();
         relatesToImpl.setValue(relatesTo.getRelatesTo());
         relatesToImpl.setRelationshipType(relatesTo.getType().toString());
         implementation.setRelatesTo(relatesToImpl);
      }
      else
      {
         implementation.setRelatesTo(null);
      }
   }

   public void addReferenceParameter(Element refParam)
   {
      EndpointReferenceType eprt = implementation.getToEndpointReference();
      ReferenceParametersType refParams = eprt.getReferenceParameters();
      if (refParams == null)
      {
         refParams = new ReferenceParametersType();
         eprt.setReferenceParameters(refParams);
      }
      eprt.getReferenceParameters().getAny().add(refParam);
      //implementation.getToEndpointReference().getReferenceParameters().getAny().add(refParam);
   }

   public void initializeAsDestination(MAPEndpoint epref)
   {
      if (epref == null)
         throw MESSAGES.invalidNullEndpointReference();

      if (epref instanceof CXFMAPEndpoint)
      {
         implementation.setTo(((CXFMAPEndpoint)epref).getImplementation());
      }
      else
      {
         throw MESSAGES.unsupportedMapEndpoin(epref);
      }
   }

   public List<Object> getReferenceParameters()
   {
      List<Object> list = new LinkedList<Object>();
      ReferenceParametersType refParams = implementation.getToEndpointReference().getReferenceParameters();
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

   public void installOutboundMapOnClientSide(Map<String, Object> requestContext, MAP map)
   {
      if (!(map instanceof CXFMAP))
      {
         throw MESSAGES.unsupportedMap(map);
      }
      AddressingProperties addressingProperties = ((CXFMAP)map).implementation;

      requestContext.put(CXFMAPConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
      requestContext.put(CXFMAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
   }

   public void installOutboundMapOnServerSide(Map<String, Object> requestContext, MAP map)
   {
      if (!(map instanceof CXFMAP))
      {
         throw MESSAGES.unsupportedMap(map);
      }
      AddressingProperties addressingProperties = ((CXFMAP)map).implementation;

      requestContext.put(CXFMAPConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
   }

}
