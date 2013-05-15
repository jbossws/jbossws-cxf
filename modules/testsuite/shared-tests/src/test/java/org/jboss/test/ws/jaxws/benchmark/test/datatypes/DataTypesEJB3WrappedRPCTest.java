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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.ws.jaxws.benchmark.Runner;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
public abstract class DataTypesEJB3WrappedRPCTest extends DataTypesTest
{
   protected String endpointURL = "http://" + Runner.getServerAddress()
         + "/jaxws-benchmark-datatypes-ejb3/EndpointEJB3WrappedRPCImpl";

   protected String targetNS = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/";

   public Object prepare() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointWrappedRPCService");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointWrappedRPC.class);
   }

   public abstract void performIteration(Object port) throws Exception;
}
