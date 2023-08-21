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

import jakarta.activation.DataHandler;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.ejb.Stateless;
import jakarta.xml.bind.annotation.XmlAttachmentRef;
import jakarta.xml.ws.WebServiceException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

@Stateless
@WebService(name = "WrappedEndpoint", serviceName = "WrappedEndpointService")
@WebContext(contextRoot = "jaxws-swaref")
public class WrappedEndpointImpl implements WrappedEndpoint
{
   @WebMethod
   public DocumentPayload beanAnnotation(DocumentPayload dhw, String test) 
   {
      DataHandler dh;
      
      try {
         System.out.println("[TestServiceImpl] ---> Dans le service");

         // récupère la pièce attachée
         if (dhw != null && dhw.getData() != null) {
            dh=dhw.getData();
            dumpDH(dh);
         }
         else
         {
            System.out.println("[TestServiceImpl] ---> Le DataHandler est NULL.");
         }
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      dh = new DataHandler("Server data", "text/plain") ;

      try{
         System.out.println("[TestServiceImpl] ---> Le DataHandler returned.");
         dumpDH(dh);
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      return new DocumentPayload(dh);
   }

   @WebMethod
   public DocumentPayloadWithList listAnnotation(DocumentPayloadWithList dhw, String test) 
   {
      DataHandler dh;
      
      try {
         System.out.println("[TestServiceImpl] ---> Dans le service");

         if (dhw != null && dhw.getData() != null && dhw.getData().get(0) != null) {
            dh=dhw.getData().get(0);
            dumpDH(dh);
         }
         else
         {
            System.out.println("[TestServiceImpl] ---> Le DataHandler est NULL.");
         }
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      dh = new DataHandler("Server data", "text/plain") ;

      try{
         System.out.println("[TestServiceImpl] ---> Le DataHandler returned.");
         dumpDH(dh);
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }

      DocumentPayloadWithList payload = new DocumentPayloadWithList();
      payload.getData().add(dh);

      return payload;
   }


   @WebMethod
   @XmlAttachmentRef
   public DataHandler parameterAnnotation(DocumentPayload payload, String test, @XmlAttachmentRef DataHandler data)
   {
      try
      {
         Object dataContent = data.getContent();
         System.out.println("Got " + dataContent);
         if (dataContent instanceof InputStream)
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

   private static void dumpDH(DataHandler in_dh) throws Exception
   {
      InputStream is = in_dh.getInputStream();
      if (is != null) {
         System.out.println("[TestServiceImpl] ---> in_dh START : ");
         // récupère le contenu du fichier
         BufferedReader in = null;
         try
         {
            in = new BufferedReader(new InputStreamReader(is));
            String ligne="";
            ligne = in.readLine();
            while (ligne != null)
            {
               System.out.println(ligne);
               ligne = in.readLine();
            }
         }
         finally
         {
            if (in != null) in.close();
         }
         System.out.println("[TestServiceImpl] ---> END.");
      }
      else
      {
         System.out.println("[TestServiceImpl] ---> in_dh inputstream is null.");
      }

   }
}
