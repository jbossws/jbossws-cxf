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

import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * Test handker to test MTOM detection.
 *
 * @author darran.lofthouse@jboss.com
 * @since 27th March 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2259
 */
public class CustomHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static final Logger log = Logger.getLogger(CustomHandler.class);

   @Override
   public boolean handleMessage(final SOAPMessageContext msgContext)
   {

      SOAPMessage soapMessage = msgContext.getMessage();
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         soapMessage.writeTo(baos);
         log.info("Wrote message.");
      }
      catch (Exception e)
      {
         throw new WebServiceException("Unable to write message.", e);
      }

      return true;
   }
}
