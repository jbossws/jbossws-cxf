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
package org.jboss.test.ws.jaxws.jbws2074.handler;

import javax.annotation.PostConstruct;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

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
public class DescriptorResourcesHandler extends GenericSOAPHandler<LogicalMessageContext>
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
   public boolean handleOutbound(MessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Inbound");
   }

   private boolean ensureInjectionsAndInitialization(MessageContext msgContext, String direction)
   {
      if (!this.correctState)
      {
         throw new WebServiceException("Unfunctional javax.annotation.* annotations");
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
