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
package org.jboss.test.ws.jaxws.benchmark.test.basic;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
