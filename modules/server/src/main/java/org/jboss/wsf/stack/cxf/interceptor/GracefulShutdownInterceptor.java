/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.RejectionRule;

/**
 * An interceptor that rejects message by throwing a Fault when the server is suspended.
 * 
 * @author alessio.soldano@jboss.com
 * @since 4-Nov-2016
 *
 */
public class GracefulShutdownInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static final Logger LOG = LogUtils.getL7dLogger(GracefulShutdownInterceptor.class);

   public GracefulShutdownInterceptor()
   {
      super(Phase.READ);
      addAfter(ReadHeadersInterceptor.class.getName());
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      ServletRequest req = (ServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
      if (req != null && "true".equals(req.getAttribute("org.wildfly.suspended")))
      {
         if (message instanceof SoapMessage)
         {
            SoapMessage soapMessage = (SoapMessage)message;
            if (!soapMessage.hasHeaders())
            {
               throw createFault();
            }
            else
            {
               Deployment dep = soapMessage.getExchange().get(Endpoint.class).getService().getDeployment();
               RejectionRule rr = dep.getAttachment(RejectionRule.class);
               if (rr != null)
               {
                  List<Header> headers = soapMessage.getHeaders();
                  Map<QName, Object> m = new HashMap<>();
                  for (Header header : headers)
                  {
                     m.put(header.getName(), header.getObject());
                  }
                  if (rr.rejectMessage(m))
                  {
                     throw createFault();
                  }
               }
            }
         }
         else
         {
            throw createFault();
         }
      }
   }

   private Fault createFault() {
      Fault f = new Fault(new org.apache.cxf.common.i18n.Message("Server is suspended", LOG));
      f.setStatusCode(503);
      return f;
   }
}
