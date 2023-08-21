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
package org.jboss.test.ws.jaxws.samples.swaref;

import org.jboss.ws.api.annotation.WebContext;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.soap.SOAPBinding;
import jakarta.ejb.Stateless;
import jakarta.activation.DataHandler;
import jakarta.xml.bind.annotation.XmlAttachmentRef;
import jakarta.xml.ws.WebServiceException;

import java.io.IOException;
import java.io.InputStream;

@Stateless
@WebService(name="BareEndpoint", serviceName="BareEndpointService")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
@WebContext(contextRoot = "jaxws-swaref")
public class BareEndpointImpl implements BareEndpoint
{
   @WebMethod
   public DocumentPayload beanAnnotation(DocumentPayload payload)
   {
      try
      {
         Object dataContent = payload.getData().getContent();
         System.out.println("Got '" + dataContent +"'");
         if (dataContent instanceof InputStream)
         {
            ((InputStream)dataContent).close();
         }
         return new DocumentPayload( new DataHandler("Server data", "text/plain"));
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }

   @WebMethod
   @XmlAttachmentRef
   public DocumentPayloadWithoutRef parameterAnnotation(@XmlAttachmentRef DocumentPayloadWithoutRef payload)
   {
      try
      {
         if(null == payload) throw new WebServiceException("Payload was null");
         Object dataContent = payload.getData().getContent();
         System.out.println("Got " + dataContent);
         if (dataContent instanceof InputStream)
         {
            ((InputStream)dataContent).close();
         }
         return new DocumentPayloadWithoutRef(new DataHandler("Server data", "text/plain"));
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
}
