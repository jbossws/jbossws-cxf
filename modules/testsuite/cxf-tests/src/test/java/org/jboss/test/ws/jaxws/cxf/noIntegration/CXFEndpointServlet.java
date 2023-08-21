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
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import jakarta.servlet.ServletConfig;
import jakarta.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class CXFEndpointServlet extends CXFNonSpringServlet
{

   private static final long serialVersionUID = -7460429764356482207L;

   @Override
   public void loadBus(ServletConfig servletConfig)
   {
      super.loadBus(servletConfig);

      // You could add the endpoint publish codes here
      try {
         //be sure to use the bus that's been created in loadBus..
         Bus bus = getBus();
         BusFactory.setThreadDefaultBus(bus);
         Endpoint.publish("/Echo1", new EchoImpl());
      } finally {
         //free the thread default bus association in the current thread which
         //is serving the servlet init, as it can have side effect on other
         //servlet(s) deployed afterwards
         BusFactory.setThreadDefaultBus(null);
      }
   }
}
