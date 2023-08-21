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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import jakarta.activation.DataHandler;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Heiko.Braun@jboss.com
 */
@WebService(name = "WrappedEndpoint", serviceName = "WrappedService")
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class WrappedEndpointImpl implements WrappedEndpoint
{

   @WebMethod
   @XmlMimeType("text/plain")
   public DataHandler parameterAnnotation(@XmlMimeType("text/plain") DataHandler data)
   {
      try
      {
         System.out.println("Recv " + data.getContentType());
         Object dataContent = data.getContent();
         System.out.println("Got " + dataContent);
         if ( dataContent instanceof InputStream )
         {
            ((InputStream)dataContent).close();
         }
         return new DataHandler("Server data", "text/plain");
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
}
