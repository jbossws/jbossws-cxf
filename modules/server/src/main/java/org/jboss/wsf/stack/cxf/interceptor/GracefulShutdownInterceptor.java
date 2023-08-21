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
package org.jboss.wsf.stack.cxf.interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.ServletRequest;
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
