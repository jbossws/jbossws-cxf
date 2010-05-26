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
package org.jboss.test.ws.jaxws.cxf.configuration;

import java.util.List;

import org.apache.cxf.binding.soap.interceptor.RPCInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * The cxf interceptor to change the request "Hello" to "ChangedRequest"
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class ChangeRequestnterceptor extends AbstractPhaseInterceptor<Message>
{
   public ChangeRequestnterceptor()
   {
      super(Phase.UNMARSHAL);
      addAfter(RPCInInterceptor.class.getName());     
   }
   
   public void handleMessage(final Message message) {
      MessageContentsList parameters = (MessageContentsList)message.getContent(List.class);
      parameters.set(0, "ChangedRequest");  
   }

}
