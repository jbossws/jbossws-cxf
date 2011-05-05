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

import org.apache.cxf.ws.addressing.AddressingConstants;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.jboss.ws.api.addressing.MAPConstants;

/**
 * MAPConstants is a wrapper which works with class MAP. This is the JBossWS CXF version.
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 26-May-2009
 *
 */
public class CXFMAPConstants implements MAPConstants
{
   public static final String CLIENT_ADDRESSING_PROPERTIES = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;
   public static final String CLIENT_ADDRESSING_PROPERTIES_INBOUND = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
   public static final String CLIENT_ADDRESSING_PROPERTIES_OUTBOUND = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
   public static final String SERVER_ADDRESSING_PROPERTIES_INBOUND = JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
   public static final String SERVER_ADDRESSING_PROPERTIES_OUTBOUND = JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;

   private AddressingConstants implementation;
   

   CXFMAPConstants(AddressingConstants implementation)
   {
      this.implementation = implementation;
   }

   public String getClientAddressingProperties()
   {
      return CLIENT_ADDRESSING_PROPERTIES;
   }

   public String getClientAddressingPropertiesInbound()
   {
      return CLIENT_ADDRESSING_PROPERTIES_INBOUND;
   }

   public String getClientAddressingPropertiesOutbound()
   {
      return CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
   }

   public String getNoneURI()
   {
      return implementation.getNoneURI();
   }
   
   public String getAnonymousURI()
   {
      return implementation.getAnonymousURI();
   }

   public String getServerAddressingPropertiesInbound()
   {
      return SERVER_ADDRESSING_PROPERTIES_INBOUND;
   }

   public String getServerAddressingPropertiesOutbound()
   {
      return SERVER_ADDRESSING_PROPERTIES_OUTBOUND;
   }

}
