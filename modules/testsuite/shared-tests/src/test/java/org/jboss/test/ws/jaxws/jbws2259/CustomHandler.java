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

import java.io.ByteArrayOutputStream;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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
