/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client;

import static org.jboss.wsf.stack.cxf.client.Constants.JBWS_CXF_JAXWS_CLIENT_BUS_SPRING_DESCRIPTOR;

import org.apache.cxf.Bus;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;

/**
 * A ClientBusSelector extension to be used with Spring integration for processing
 * a different (cxf-client.xml) Bus descriptor when building JAX-WS clients.
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Oct-2014
 *
 */
@Deprecated
public class SpringCustomClientBusSelector extends ClientBusSelector
{
   private static final String clientBusDescriptor = SecurityActions.getSystemProperty(JBWS_CXF_JAXWS_CLIENT_BUS_SPRING_DESCRIPTOR, "cxf-client.xml");
   
   @Override
   public Bus createNewBus() {
      return new JBossWSBusFactory().createBus(clientBusDescriptor);
   }
   
}
