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
   private final EndpointReferenceType implementation;

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
