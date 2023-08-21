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
