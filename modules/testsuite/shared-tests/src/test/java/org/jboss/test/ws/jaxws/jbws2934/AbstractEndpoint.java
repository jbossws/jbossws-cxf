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
