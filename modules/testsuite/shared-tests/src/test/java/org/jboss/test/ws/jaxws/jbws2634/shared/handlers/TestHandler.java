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
package org.jboss.test.ws.jaxws.jbws2634.shared.handlers;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.jbws2634.shared.BeanIface;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * This handler is initialized via injections.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class TestHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // provide logging
   private static final Logger log = Logger.getLogger(TestHandler.class);

   @Resource(name="boolean1")
   private Boolean boolean1;

   @EJB
   private BeanIface bean1;

   /**
    * Indicates whether handler is in correct state.
    */
   private boolean correctState;

   @PostConstruct
   private void init()
   {
      boolean correctInitialization = true;

      // verify @Resource annotation driven injection
      if (this.boolean1 == null || this.boolean1 != true)
      {
         log.error("Annotation driven initialization for boolean1 failed");
         correctInitialization = false;
      }
      // verify @EJB annotation driven injection
      if (this.bean1 == null || !this.bean1.printString().equals("Injected hello message"))
      {
         log.error("Annotation driven initialization for bean1 failed");
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
         throw new WebServiceException("Unfunctional injections");
      }

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
         soapElement = (SOAPElement)soapElement.getChildElements().next();

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":TestHandler";
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
