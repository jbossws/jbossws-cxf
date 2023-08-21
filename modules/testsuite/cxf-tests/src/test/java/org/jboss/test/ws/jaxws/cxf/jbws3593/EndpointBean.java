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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import static jakarta.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import jakarta.activation.DataHandler;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceException;

import org.apache.cxf.interceptor.OutInterceptors;
import org.jboss.logging.Logger;

@WebService(name="Endpoint", targetNamespace="http://org.jboss.ws/jaxws/jbws3593")
@SOAPBinding(style = SOAPBinding.Style.RPC, parameterStyle = SOAPBinding.ParameterStyle.BARE)
@BindingType(SOAP11HTTP_MTOM_BINDING)
@OutInterceptors(interceptors = {"org.jboss.test.ws.jaxws.cxf.jbws3593.MTOMOutInterceptor"})
public class EndpointBean
{
   private static Logger log = Logger.getLogger(EndpointBean.class);
   
   @XmlMimeType("text/plain")
   public DataHandler namespace(@XmlMimeType("text/plain") DataHandler data)
   {
      try
      {
         String name = (String)getContent(data);
         String type = (String)data.getContentType();
         log.info("User " + name + " requested namespace with content type ["+ type +"]");

         return new DataHandler("Hello " + name, "text/plain");
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
   
   public List<String> echoStrings(List<String> l) {
      if (l != null) {
         for (String s : l) {
            log.info("String: " + s);
         }
      }
      return l;
   }

   protected Object getContent(DataHandler dh) throws IOException
   {
      Object content = dh.getContent();

      if (content instanceof InputStream)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
            return br.readLine();
         }
         finally
         {
            ((InputStream)content).close();
         }
      }
      return content;
   }
}
