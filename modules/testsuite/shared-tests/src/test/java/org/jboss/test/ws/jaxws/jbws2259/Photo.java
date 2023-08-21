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

import jakarta.activation.DataHandler;
import jakarta.xml.bind.annotation.XmlMimeType;

/**
 * Representation of a photo to test marshalling.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 27th March 2009
 */
public class Photo
{

   private String caption;

   private String expectedContentType;
   
   private DataHandler image;

   public String getCaption()
   {
      return caption;
   }

   public void setCaption(String caption)
   {
      this.caption = caption;
   }

   public String getExpectedContentType()
   {
      return expectedContentType;
   }

   public void setExpectedContentType(String expectedContentType)
   {
      this.expectedContentType = expectedContentType;
   }

   @XmlMimeType("*/*")
   public DataHandler getImage()
   {
      return image;
   }

   public void setImage(DataHandler image)
   {
      this.image = image;
   }

}
