/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.benchmark.test.basic;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.ws.jaxws.benchmark.BenchmarkTest;
import org.jboss.test.ws.jaxws.benchmark.Runner;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Sep-2009
 *
 */
public class BasicTest implements BenchmarkTest
{
   private String endpointURL = "http://" + Runner.getServerAddress() + "/jaxws-benchmark-basic/EndpointService/EndpointImpl";
   private String targetNS = "http://basic.test.benchmark.jaxws.ws.test.jboss.org/";

   public Object prepare() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Endpoint.class);
   }

   public void performIteration(Object port) throws Exception
   {
      String par = "Hello" + Math.random();
      String ret = ((Endpoint)port).echo(par);
      if (!(par.equals(ret)))
      {
         throw new Exception("Unexpected result: " + ret);
      }
   }

}
