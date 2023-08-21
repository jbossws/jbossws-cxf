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
package org.jboss.test.ws.jaxws.jbws2074.usecase5.service;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.api.annotation.WebContext;

@Stateless
@WebService
(
   name = "EJB3",
   serviceName = "EJB3Service",
   targetNamespace = "http://ws.jboss.org/jbws2074",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws2074.usecase5.service.EJB3Iface"
)
@EndpointConfig(configFile = "endpoint-config.xml", configName = "MyConf")
@WebContext
(
   urlPattern="/Service", 
   contextRoot="/jaxws-jbws2074-usecase5"
)
public class EJB3Impl implements EJB3Iface
{

   private static Logger log = Logger.getLogger(EJB3Impl.class);

   public String echo(String msg)
   {
      log.info("echo: " + msg);
      return msg + ":EJB3Impl";
   }

}
