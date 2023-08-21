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
package org.jboss.test.ws.management.recording;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.api.annotation.WebContext;

/**
 * author alessio.soldano@jboss.com
 * @since 20-Jul-2011
 */
@Stateless
@WebService(serviceName="EndpointService", endpointInterface = "org.jboss.test.ws.management.recording.Endpoint")
@WebContext(contextRoot="/management-recording", urlPattern="/EndpointImpl")
@EndpointConfig(configName="Recording-Endpoint-Config")
public class EndpointWithConfigImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointWithConfigImpl.class);

   public String echo1(String input)
   {
      log.info("echo1: " + input);
      return input;
   }
   
   public String echo2(String input)
   {
      log.info("echo2: " + input);
      return input;
   }
}
