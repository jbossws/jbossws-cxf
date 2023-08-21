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
package org.jboss.test.ws.jaxws.jbws1822.webservice;

import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1822.shared.BeanIface;
import org.jboss.ws.api.annotation.WebContext;

/**
 * EJB3 bean published as WebService injecting other EJB3 bean
 *
 * @author richard.opalka@jboss.org
 * 
 * @since 08-Jan-2008
 */
@WebService(targetNamespace = "http://jbossws.org/JBWS1822", serviceName = "EndpointService", endpointInterface="org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface")
@WebContext(contextRoot="/jaxws-jbws1822", urlPattern="/*")
@Remote(EJB3RemoteIface.class)
@Stateless
public class EJB3Bean implements EJB3RemoteIface
{
   @EJB
   private BeanIface testBean;
    
   public String getMessage()
   {
      return testBean.printString();
   }
}
