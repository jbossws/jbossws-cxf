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
package org.jboss.test.ws.jaxws.jbws2934;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;

/**
 * Abstract endpoint implementation reused in both JSE and EJB endpoint.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
abstract class AbstractEndpoint implements Endpoint
{
 
   protected AbstractEndpoint()
   {
      super();
   }
   
   @Resource
   WebServiceContext wsCtx1;
   WebServiceContext wsCtx2;

   @Resource
   void setWebServiceContext(WebServiceContext wsCtx)
   {
      this.wsCtx2 = wsCtx;
   }
   
   @PostConstruct
   private void init()
   {
      this.assertWebServiceContexts();
   }
   
   protected int getQueryParameterInternal(String key)
   {
      this.assertWebServiceContexts();
      int ctx1Value = this.getValue(this.wsCtx1, key);
      int ctx2Value = this.getValue(this.wsCtx2, key);
      if (ctx1Value != ctx2Value)
         throw new WebServiceException("Values have to be equivalent, they're coming from the same request");

      return ++ctx1Value; 
   }
   
   protected int getValue(WebServiceContext wsCtx, String queryKey)
   {
      HttpServletRequest servletReq = (HttpServletRequest)wsCtx.getMessageContext().get(MessageContext.SERVLET_REQUEST);
      String queryString = servletReq.getQueryString();
      int equalsSignPosition = queryString.indexOf('=');
      return Integer.valueOf(queryString.substring(equalsSignPosition + 1));
   }
   
   protected void assertWebServiceContexts()
   {
      if (this.wsCtx1 == null)
         throw new WebServiceException("Web service context 1 is null");
      if (this.wsCtx2 == null)
         throw new WebServiceException("Web service context 2 is null");
   }
}
