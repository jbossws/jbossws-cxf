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
package org.jboss.test.ws.jaxws.jbws2074.handler;

import jakarta.annotation.PostConstruct;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * This handler is initialized via injections.
 * Injections can be specified in both web.xml
 * and ejb-jar.xml. Thus @Resource annotation
 * is ommited here.
 *
 * @author ropalka@redhat.com
 */
public class DescriptorResourcesHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // provide logging
   private static final Logger log = Logger.getLogger(DescriptorResourcesHandler.class);

   /**
    * java.lang.Boolean
    */

   // XML driven injection
   private Boolean boolean0;

   private Boolean _boolean1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setBoolean1(Boolean b)
   {
      this._boolean1 = b;
   }

   /**
    * java.lang.Byte
    */

   // XML driven injection
   private Byte byte0;

   private Byte _byte1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setByte1(Byte b)
   {
      this._byte1 = b;
   }

   /**
    * java.lang.Character
    */

   // XML driven injection
   private Character character0;

   private Character _character1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setCharacter1(Character c)
   {
      this._character1 = c;
   }

   /**
    * java.lang.Short
    */

   // XML driven injection
   private Short short0;

   private Short _short1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setShort1(Short i)
   {
      this._short1 = i;
   }

   /**
    * java.lang.Integer
    */

   // XML driven injection
   private Integer integer0;

   private Integer _integer1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setInteger1(Integer i)
   {
      this._integer1 = i;
   }

   /**
    * java.lang.Long
    */

   // XML driven injection
   private Long long0;

   private Long _long1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setLong1(Long l)
   {
      this._long1 = l;
   }

   /**
    * java.lang.Float
    */

   // XML driven injection
   private Float float0;

   private Float _float1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setFloat1(Float f)
   {
      this._float1 = f;
   }

   /**
    * java.lang.Double
    */

   // XML driven injection
   private Double double0;

   private Double _double1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setDouble1(Double d)
   {
      this._double1 = d;
   }

   /**
    * java.lang.String
    */

   // XML driven injection
   private String string0;

   private String string1;

   // XML driven injection
   @SuppressWarnings("unused")
   private void setString1(String s)
   {
      this.string1 = s;
   }

   /**
    * Indicates whether handler is in correct state.
    */
   private boolean correctState;

   @PostConstruct
   private void init()
   {
      boolean correctInitialization = true;

      // java.lang.Boolean
      if (this.boolean0 == null || this.boolean0 != true)
      {
         log.error("Descriptor driven initialization for boolean0 failed");
         correctInitialization = false;
      }
      if (this._boolean1 == null || this._boolean1 != true)
      {
         log.error("Descriptor driven initialization for boolean1 failed");
         correctInitialization = false;
      }

      // java.lang.Byte
      if (this.byte0 == null || this.byte0 != (byte)1)
      {
         log.error("Descriptor driven initialization for byte0 failed");
         correctInitialization = false;
      }
      if (this._byte1 == null || this._byte1 != (byte)1)
      {
         log.error("Descriptor driven initialization for byte1 failed");
         correctInitialization = false;
      }

      // java.lang.Character
      if (this.character0 == null || this.character0 != 'c')
      {
         log.error("Descriptor driven initialization for character0 failed");
         correctInitialization = false;
      }
      if (this._character1 == null || this._character1 != 'c')
      {
         log.error("Descriptor driven initialization for character1 failed");
         correctInitialization = false;
      }

      // java.lang.Short
      if (this.short0 == null || this.short0 != (short)5)
      {
         log.error("Descriptor driven initialization for short0 failed");
         correctInitialization = false;
      }
      if (this._short1 == null || this._short1 != (short)5)
      {
         log.error("Descriptor driven initialization for short1 failed");
         correctInitialization = false;
      }

      // java.lang.Integer
      if (this.integer0 == null || this.integer0 != 7)
      {
         log.error("Descriptor driven initialization for integer0 failed");
         correctInitialization = false;
      }
      if (this._integer1 == null || this._integer1 != 7)
      {
         log.error("Descriptor driven initialization for integer1 failed");
         correctInitialization = false;
      }

      // java.lang.Long
      if (this.long0 == null || this.long0 != 11L)
      {
         log.error("Descriptor driven initialization for long0 failed");
         correctInitialization = false;
      }
      if (this._long1 == null || this._long1 != 11L)
      {
         log.error("Descriptor driven initialization for long1 failed");
         correctInitialization = false;
      }

      // java.lang.Float
      if (this.float0 == null || this.float0 != 13.0f)
      {
         log.error("Descriptor driven initialization for float0 failed");
         correctInitialization = false;
      }
      if (this._float1 == null || this._float1 != 13.0f)
      {
         log.error("Descriptor driven initialization for float1 failed");
         correctInitialization = false;
      }

      // java.lang.Double
      if (this.double0 == null || this.double0 != 17.0)
      {
         log.error("Descriptor driven initialization for double0 failed");
         correctInitialization = false;
      }
      if (this._double1 == null || this._double1 != 17.0)
      {
         log.error("Descriptor driven initialization for double1 failed");
         correctInitialization = false;
      }

      // java.lang.String
      if ("s".equals(this.string0) == false)
      {
         log.error("Descriptor driven initialization for string0 failed");
         correctInitialization = false;
      }
      if ("s".equals(this.string1) == false)
      {
         log.error("Descriptor driven initialization for string1 failed");
         correctInitialization = false;
      }

      this.correctState = correctInitialization;
   }

   @Override
   public boolean handleOutbound(SOAPMessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Inbound");
   }

   private boolean ensureInjectionsAndInitialization(MessageContext msgContext, String direction)
   {
      if (!this.correctState)
      {
         throw new WebServiceException("Unfunctional jakarta.annotation.* annotations");
      }

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
         soapElement = (SOAPElement)soapElement.getChildElements().next();

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":DescriptorResourcesHandler";
         soapElement.setValue(newValue);

         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);

         return true;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

}
