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
package org.jboss.test.ws.jaxws.jbws2259;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceException;
import jakarta.activation.DataHandler;

import org.jboss.logging.Logger;

/**
 * Test Endpoint to test MTOM detection.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 27th March 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2259
 */
@WebService(name = "Endpoint", serviceName = "EndpointService", targetNamespace = "http://ws.jboss.org/jbws2259", endpointInterface = "org.jboss.test.ws.jaxws.jbws2259.Endpoint")
@BindingType(value="http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class EndpointImpl implements Endpoint
{

   private static final Logger log = Logger.getLogger(EndpointImpl.class);

   public Photo echo(Photo photo)
   {

      DataHandler dh = photo.getImage();
      String contentType = dh.getContentType();
      log.info("Actual content-type " + contentType);
      String expectedContentType = photo.getExpectedContentType();
      log.info("Expected content-type " + expectedContentType);

      if (expectedContentType.equals(contentType) == false)
      {
         throw new WebServiceException("Expected content-type '" + expectedContentType + "' Actual content-type '" + contentType + "'");
      }

      try
      {
         Object content = dh.getContent();
         log.info("Content - " + content.toString());
         if ( !(content instanceof Image) && !(content instanceof InputStream))
         {
            throw new WebServiceException("Unexpected content '" + content.getClass().getName() + "'");
         }
      }
      catch (IOException e)
      {
         throw new WebServiceException("Unable to getContent()", e);
      }

      return photo;
   }

}
