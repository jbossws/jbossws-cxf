/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.namespace;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.test.helper.DOMWriter;

/**
 * A simple SOAPHandler checking the exchanged message uses the SEI namespace.
 *
 * @author alessio.soldano@jboss.com
 * @since 04-Jul-2008
 */
public class CustomHandler implements SOAPHandler<SOAPMessageContext>
{
   private static final Logger log = Logger.getLogger(CustomHandler.class);

   @Override
   public Set<QName> getHeaders()
   {
      //don't care
      return null;
   }

   @Override
   public void close(MessageContext arg0)
   {
      //nothing to do
   }

   @Override
   public boolean handleFault(SOAPMessageContext arg0)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean handleMessage(SOAPMessageContext context)
   {
      log.debug("handleMessage...");
      try
      {
         SOAPMessageContext msgContext = context;
         SOAPBody body = msgContext.getMessage().getSOAPBody();
         String bodyStr = DOMWriter.printNode(body, false);
         if (bodyStr.indexOf("http://example.org/sei") < 0)
            throw new WebServiceException("Invalid body: " + bodyStr);
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
      return true;
   }

}
