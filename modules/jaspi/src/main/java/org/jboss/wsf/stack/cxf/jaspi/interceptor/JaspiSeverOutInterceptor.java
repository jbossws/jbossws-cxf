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
package org.jboss.wsf.stack.cxf.jaspi.interceptor;

import java.util.ListIterator;

import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.stack.cxf.jaspi.JaspiServerAuthenticator;

/** 
 * CXF out interceptor to secureResponse cxf SoapMessage with JaspiServerAuthenticator
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaspiSeverOutInterceptor extends AbstractSoapInterceptor
{
   private final JaspiServerAuthenticator authManager;
   private static final SAAJOutInterceptor SAAJ_OUT = new SAAJOutInterceptor();

   public JaspiSeverOutInterceptor(JaspiServerAuthenticator authManager)
   {
      super(Phase.PRE_STREAM);
      addAfter(StaxOutInterceptor.class.getName());
      this.authManager = authManager;
   }

   @Override
   public void handleMessage(SoapMessage message) throws Fault
   {
      if (!chainAlreadyContainsSAAJ(message))
      {
         SAAJ_OUT.handleMessage(message);
      }
      message.getInterceptorChain().add(new JaspiServerOutEndingInterceptor());

   }

   private static boolean chainAlreadyContainsSAAJ(SoapMessage message)
   {
      ListIterator<Interceptor<? extends Message>> listIterator = message.getInterceptorChain().getIterator();
      while (listIterator.hasNext())
      {
         if (listIterator.next() instanceof SAAJOutInterceptor)
         {
            return true;
         }
      }
      return false;
   }

   public class JaspiServerOutEndingInterceptor extends AbstractSoapInterceptor
   {
      public JaspiServerOutEndingInterceptor()
      {
         super(Phase.WRITE_ENDING);
         addAfter(SoapOutInterceptor.SoapOutEndingInterceptor.class.getName());
      }

      @Override
      public void handleMessage(SoapMessage message) throws Fault
      {
         if (message.getContent(SOAPMessage.class) == null)
         {
            return;
         }
         authManager.secureResponse(message);
      }
   }

}
