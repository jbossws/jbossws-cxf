/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1733;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

@WebService(serviceName = "JBWS1733Service", endpointInterface = "org.jboss.test.ws.jaxws.jbws1733.JBWS1733")
public class JBWS1733Impl implements JBWS1733
{

   @Resource
   private WebServiceContext wsContext;

   public int getCounter()
   {
      MessageContext mc = wsContext.getMessageContext();
      HttpSession session = ((HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST)).getSession();
      
      // Get a session property "counter" from context
      if (session == null)
         throw new WebServiceException("No session in WebServiceContext");
      
      Integer counter = (Integer)session.getAttribute("counter");
      if (counter == null)
      {
         counter = Integer.valueOf(0);
         System.out.println("Starting the Session");
      }
      counter = Integer.valueOf(counter.intValue() + 1);
      session.setAttribute("counter", counter);
      return counter;
   }

}
