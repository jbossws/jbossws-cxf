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
package org.jboss.test.ws.jaxws.namespace;

import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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
