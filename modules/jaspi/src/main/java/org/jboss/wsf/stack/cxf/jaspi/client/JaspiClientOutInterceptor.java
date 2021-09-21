/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.jaspi.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor.SAAJPreInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

/** 
 * CXF out interceptor to secureRequest cxf SoapMessage with JaspiClientAuthentcator
 * @See org.jboss.wsf.stack.cxf.client.jaspi.JaspiClientAuthentcator
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaspiClientOutInterceptor extends AbstractSoapInterceptor
{
   private final JaspiClientAuthenticator authManager;

   public JaspiClientOutInterceptor(JaspiClientAuthenticator authManager)
   {
      super(Phase.PRE_PROTOCOL);
      addAfter(SAAJInInterceptor.class.getName());
      this.authManager = authManager;
   }

   @Override
   public void handleMessage(SoapMessage message) throws Fault
   {
      if (message.getContent(SOAPMessage.class) == null)
      {
         SAAJInInterceptor saajIn = new SAAJInInterceptor();
         saajIn.handleMessage(message);
      }
      SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
      if (soapMessage == null)
      {
         return;
      }

      SOAPMessage copyMessage = null;
      try
      {
         MessageFactory messageFactory = SAAJPreInInterceptor.INSTANCE.getFactory(message);
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         soapMessage.writeTo(bout);
         copyMessage = messageFactory.createMessage(soapMessage.getMimeHeaders(),
               new ByteArrayInputStream(bout.toByteArray()));
      }
      catch (SOAPException e)
      {
         throw new Fault(e);
      }
      catch (IOException e)
      {
         throw new Fault(e);
      }
      if (copyMessage != null)
      {
         message.put(SOAPMessage.class, copyMessage);
      }
      try
      {
         authManager.secureRequest(message);
      }
      finally
      {
         message.put(SOAPMessage.class, soapMessage);
      }

   }

}
