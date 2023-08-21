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
package org.jboss.test.ws.jaxws.jbws2250;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;

/**
 * Test type to be marshalled.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 7th July 2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Message
{

   @XmlElementRef(name = "myId", type = JAXBElement.class)
   protected JAXBElement<Id> myId;

   protected String message;

   public JAXBElement<Id> getMyId()
   {
      return myId;
   }

   public void setMyId(JAXBElement<Id> myId)
   {
      this.myId = myId;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

}
