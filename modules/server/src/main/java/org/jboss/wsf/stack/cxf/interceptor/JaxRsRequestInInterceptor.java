/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import java.util.regex.Pattern;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
/**
 * interceptor to replace double slash with single slash in requsetURI 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaxRsRequestInInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static final Pattern pathPattern = Pattern.compile("/{2,}");
   public JaxRsRequestInInterceptor()
   {
      super(Phase.UNMARSHAL);
      addBefore(JAXRSInInterceptor.class.getName());
   }
   
   @Override
   public void handleMessage(Message message) throws Fault
   {
      String requestURI = (String) message.get(Message.REQUEST_URI);
      if (requestURI != null)
      {
         requestURI = pathPattern.matcher(requestURI).replaceAll("/");
         message.put(Message.REQUEST_URI, requestURI);
      }
   }
}
