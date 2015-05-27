/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.MessageInfo;

/**
 * A CXF interceptor for selectively turn off MTOM
 */
public class MTOMOutInterceptor extends AbstractPhaseInterceptor<Message>
{

   public MTOMOutInterceptor()
   {
      super(Phase.PRE_STREAM);
      addBefore(AttachmentOutInterceptor.class.getName());
   }

   public void handleMessage(Message message)
   {
      MessageInfo mi = (MessageInfo)message.get(MessageInfo.class);
      if ("echoStringsResponse".equals(mi.getName().getLocalPart())) {
         message.put("mtom-enabled", false);
      }
   }
}
