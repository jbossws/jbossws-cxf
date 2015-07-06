/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * All endpoint management interceptor should extend this class to get allowed http methods etc.
 * @author <a href="mailto:ema@redhat.com/>Jim Ma</a>
 *
 */
public abstract class AbstractManagementInterceptor extends AbstractPhaseInterceptor<Message>
{
   public AbstractManagementInterceptor(String phase)
   {
      super(phase);
      // TODO Auto-generated constructor stub
   }

   protected String getEncoding(Message message)
   {
      Exchange ex = message.getExchange();
      String encoding = (String)message.get(Message.ENCODING);
      if (encoding == null && ex.getInMessage() != null)
      {
         encoding = (String)ex.getInMessage().get(Message.ENCODING);
         message.put(Message.ENCODING, encoding);
      }

      if (encoding == null)
      {
         encoding = "UTF-8";
         message.put(Message.ENCODING, encoding);
      }
      return encoding;
   }
   
}
