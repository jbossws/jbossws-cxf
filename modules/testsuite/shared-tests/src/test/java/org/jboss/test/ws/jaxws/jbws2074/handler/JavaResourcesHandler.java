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
import jakarta.annotation.Resource;
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
 * Injections are specified via @Resource annotation
 * and refer to the ejb-jar.xml or web.xml defined
 * environment entries.
 *
 * @author ropalka@redhat.com
 */
public class JavaResourcesHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaResourcesHandler.class);

   /**
    * java.lang.Boolean
    */

   @Resource(name = "boolean")
   private Boolean boolean0;

   private Boolean _boolean1;

   @Resource(name = "boolean")
   private void setBoolean1(Boolean b)
   {
      this._boolean1 = b;
   }

   /**
    * java.lang.Byte
    */

   @Resource(name = "byte")
   private Byte byte0;

   private Byte _byte1;

   @Resource(name = "byte")
   private void setByte1(Byte b)
   {
      this._byte1 = b;
   }

   /**
    * java.lang.Character
    */

   @Resource(name = "character")
   private Character character0;

   private Character _character1;

   @Resource(name = "character")
   private void setCharacter1(Character c)
   {
      this._character1 = c;
   }

   /**
    * java.lang.Short
    */

   @Resource(name = "short")
   private Short short0;

   private Short _short1;

   @Resource(name = "short")
   private void setShort1(Short i)
   {
      this._short1 = i;
   }

   /**
    * java.lang.Integer
    */

   @Resource(name = "integer")
   private Integer integer0;

   private Integer _integer1;

   @Resource(name = "integer")
   private void setInteger1(Integer i)
   {
      this._integer1 = i;
   }

   /**
    * java.lang.Long
    */

   @Resource(name = "long")
   private Long long0;

   private Long _long1;

   @Resource(name = "long")
   private void setLong1(Long l)
   {
      this._long1 = l;
   }

   /**
    * java.lang.Float
    */

   @Resource(name = "float")
   private Float float0;

   private Float _float1;

   @Resource(name = "float")
   private void setFloat1(Float f)
   {
      this._float1 = f;
   }

   /**
    * java.lang.Double
    */

   @Resource(name = "double")
   private Double double0;

   private Double _double1;

   @Resource(name = "double")
   private void setDouble1(Double d)
   {
      this._double1 = d;
   }

   /**
    * java.lang.String
    */

   @Resource(name = "string")
   private String string;

   private String _string1;

   @Resource(name = "string")
   private void setString(String s)
   {
      this._string1 = s;
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
         log.error("@Resource initialization for boolean0 failed");
         correctInitialization = false;
      }
      if (this._boolean1 == null || this._boolean1 != true)
      {
         log.error("@Resource initialization for boolean1 failed");
         correctInitialization = false;
      }

      // java.lang.Byte
      if (this.byte0 == null || this.byte0 != (byte)1)
      {
         log.error("@Resource initialization for byte0 failed");
         correctInitialization = false;
      }
      if (this._byte1 == null || this._byte1 != (byte)1)
      {
         log.error("@Resource initialization for byte1 failed");
         correctInitialization = false;
      }

      // java.lang.Character
      if (this.character0 == null || this.character0 != 'c')
      {
         log.error("@Resource initialization for character0 failed");
         correctInitialization = false;
      }
      if (this._character1 == null || this._character1 != 'c')
      {
         log.error("@Resource initialization for character1 failed");
         correctInitialization = false;
      }

      // java.lang.Short
      if (this.short0 == null || this.short0 != (short)5)
      {
         log.error("@Resource initialization for short0 failed");
         correctInitialization = false;
      }
      if (this._short1 == null || this._short1 != (short)5)
      {
         log.error("@Resource initialization for short1 failed");
         correctInitialization = false;
      }

      // java.lang.Integer
      if (this.integer0 == null || this.integer0 != 7)
      {
         log.error("@Resource initialization for integer0 failed");
         correctInitialization = false;
      }
      if (this._integer1 == null || this._integer1 != 7)
      {
         log.error("@Resource initialization for integer1 failed");
         correctInitialization = false;
      }

      // java.lang.Long
      if (this.long0 == null || this.long0 != 11L)
      {
         log.error("@Resource initialization for long0 failed");
         correctInitialization = false;
      }
      if (this._long1 == null || this._long1 != 11L)
      {
         log.error("@Resource initialization for long1 failed");
         correctInitialization = false;
      }

      // java.lang.Float
      if (this.float0 == null || this.float0 != 13.0f)
      {
         log.error("@Resource initialization for float0 failed");
         correctInitialization = false;
      }
      if (this._float1 == null || this._float1 != 13.0f)
      {
         log.error("@Resource initialization for float1 failed");
         correctInitialization = false;
      }

      // java.lang.Double
      if (this.double0 == null || this.double0 != 17.0)
      {
         log.error("@Resource initialization for double0 failed");
         correctInitialization = false;
      }
      if (this._double1 == null || this._double1 != 17.0)
      {
         log.error("@Resource initialization for double1 failed");
         correctInitialization = false;
      }

      // java.lang.String
      if ("s".equals(this.string) == false)
      {
         log.error("@Resource initialization for string0 failed");
         correctInitialization = false;
      }
      if ("s".equals(this._string1) == false)
      {
         log.error("@Resource initialization for string1 failed");
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
         String newValue = oldValue + ":" + direction + ":JavaResourcesHandler";
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
