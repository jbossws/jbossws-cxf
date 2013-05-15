/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2259;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;

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
