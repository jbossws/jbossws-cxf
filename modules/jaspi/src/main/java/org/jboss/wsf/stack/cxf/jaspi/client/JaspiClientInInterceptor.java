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

import jakarta.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

/** 
 * CXF in interceptor to validateResponse cxf SoapMessage with JaspiClientAuthentcator
 * @See org.jboss.wsf.stack.cxf.client.jaspi.JaspiClientAuthentcator
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaspiClientInInterceptor extends AbstractSoapInterceptor
{
   private final JaspiClientAuthenticator authManager;

   public JaspiClientInInterceptor(JaspiClientAuthenticator authManager)
   {
      super(Phase.POST_PROTOCOL_ENDING);
      addAfter(SAAJOutInterceptor.SAAJOutEndingInterceptor.class.getName());
      this.authManager = authManager;
   }

   @Override
   public void handleMessage(SoapMessage message) throws Fault
   {

      if (message.getContent(SOAPMessage.class) == null)
      {
         SAAJOutInterceptor saajout = new SAAJOutInterceptor();
         saajout.handleMessage(message);
      }
      authManager.validateResponse(message);
   }

}
