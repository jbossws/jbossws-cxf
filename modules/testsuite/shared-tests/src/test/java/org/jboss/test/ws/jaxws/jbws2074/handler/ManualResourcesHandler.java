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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * This handler is initialized using manual JNDI lookup
 * and refer to the ejb-jar.xml or web.xml defined
 * environment entries.
 *
 * @author ropalka@redhat.com
 */
public class ManualResourcesHandler extends GenericSOAPHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(ManualResourcesHandler.class);

   /**
    * java.lang.Boolean
    */

   private Boolean boolean0;

   /**
    * java.lang.Byte
    */

   private Byte byte0;

   /**
    * java.lang.Character
    */

   private Character character0;

   /**
    * java.lang.Short
    */

   private Short short0;

   /**
    * java.lang.Integer
    */

   private Integer integer0;

   /**
    * java.lang.Long
    */

   private Long long0;

   /**
    * java.lang.Float
    */

   private Float float0;

   /**
    * java.lang.Double
    */

   private Double double0;

   /**
    * java.lang.String
    */

   private String string;

   /**
    * Indicates whether handler is in correct state.
    */
   private boolean correctState;

   @PostConstruct
   private void init()
   {
      doManualJndiLookup();
   }

   private void doManualJndiLookup() {
      this.boolean0 = null;
      this.byte0 = null;
      this.character0 = null;
      this.short0 = null;
      this.integer0 = null;
      this.long0 = null;
      this.float0 = null;
      this.double0 = null;
      this.string = null;
      boolean correctInitialization = true;
      try {
         final InitialContext env = new InitialContext();

         // java.lang.Boolean
         this.boolean0 = (Boolean)env.lookup("java:comp/env/boolean");
         if (this.boolean0 == null || this.boolean0 != true)
         {
            log.error("Manual JNDI lookup for boolean0 failed");
            correctInitialization = false;
         }

         // java.lang.Byte
         this.byte0 = (Byte)env.lookup("java:comp/env/byte");
         if (this.byte0 == null || this.byte0 != (byte)1)
         {
            log.error("Manual JNDI lookup for byte0 failed");
            correctInitialization = false;
         }

         // java.lang.Character
         this.character0 = (Character)env.lookup("java:comp/env/character");
         if (this.character0 == null || this.character0 != 'c')
         {
            log.error("Manual JNDI lookup for character0 failed");
            correctInitialization = false;
         }

         // java.lang.Short
         this.short0 = (Short)env.lookup("java:comp/env/short");
         if (this.short0 == null || this.short0 != (short)5)
         {
            log.error("Manual JNDI lookup for short0 failed");
            correctInitialization = false;
         }

         // java.lang.Integer
         this.integer0 = (Integer)env.lookup("java:comp/env/integer");
         if (this.integer0 == null || this.integer0 != 7)
         {
            log.error("Manual JNDI lookup for integer0 failed");
            correctInitialization = false;
         }

         // java.lang.Long
         this.long0 = (Long)env.lookup("java:comp/env/long");
         if (this.long0 == null || this.long0 != 11L)
         {
            log.error("Manual JNDI lookup for long0 failed");
            correctInitialization = false;
         }

         // java.lang.Float
         this.float0 = (Float)env.lookup("java:comp/env/float");
         if (this.float0 == null || this.float0 != 13.0f)
         {
            log.error("Manual JNDI lookup for float0 failed");
            correctInitialization = false;
         }

         // java.lang.Double
         this.double0 = (Double)env.lookup("java:comp/env/double");
         if (this.double0 == null || this.double0 != 17.0)
         {
            log.error("Manual JNDI lookup for double0 failed");
            correctInitialization = false;
         }

         // java.lang.String
         this.string = (String)env.lookup("java:comp/env/string");
         if ("s".equals(this.string) == false)
         {
            log.error("Manual JNDI lookup for string0 failed");
            correctInitialization = false;
         }

         this.correctState = correctInitialization;
      } catch (final NamingException e) {
          this.correctState = false;
      }
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
         throw new WebServiceException("Unfunctional manual JNDI lookups in @PostConstruct annotated methods");
      }
      doManualJndiLookup();
      if (!this.correctState)
      {
         throw new WebServiceException("Unfunctional manual JNDI lookups in handler execution methods");
      }

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
         soapElement = (SOAPElement)soapElement.getChildElements().next();

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":ManualResourcesHandler";
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
