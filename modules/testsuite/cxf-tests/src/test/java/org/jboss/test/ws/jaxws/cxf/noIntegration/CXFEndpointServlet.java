/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;

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
