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
package org.jboss.ws.bench;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.jboss.test.ws.jaxws.benchmark.test.basic.Endpoint;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTestHelper;

public class JAXWSBenchmark extends AbstractJavaSamplerClient
{
   private final String endpointURL = "http://" + JBossWSTestHelper.getServerHost() + ":" + JBossWSTestHelper.getServerPort() + "/jaxws-benchmark-basic/EndpointService/EndpointImpl";
   private final String targetNS = "http://basic.test.benchmark.jaxws.ws.test.jboss.org/";
   private Endpoint ep;
   
   @Override
   public void setupTest(JavaSamplerContext context) {
      super.setupTest(context);
      try {
         this.ep = prepare();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public void teardownTest(JavaSamplerContext context) {
      super.teardownTest(context);
      this.ep = null;
   }
   
   @Override
   public SampleResult runTest(JavaSamplerContext ctx)
   {
      final SampleResult sampleResult = new SampleResult();
      sampleResult.sampleStart();

      try {
         performIteration(ep);
         sampleResult.setSuccessful(true);
      } catch (Exception e) {
         sampleResult.setSuccessful(false);
         sampleResult.setResponseMessage("Exception: " + e);
         StringWriter stringWriter = new StringWriter();
         e.printStackTrace(new PrintWriter(stringWriter));
         sampleResult.setResponseData(stringWriter.toString(), "UTF-8");
         sampleResult.setDataType(SampleResult.TEXT);
      } finally {
         sampleResult.sampleEnd();
      }

      return sampleResult;
   }

   public Endpoint prepare() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      //explicitly use JBossWS-CXF JAXWS SPI Provider impl as jmeter uses a flat classpath approach
      //and it's not possible to configure the jar order
      ProviderImpl p = new ProviderImpl();
      return p.createServiceDelegate(wsdlURL, serviceName, null).getPort(Endpoint.class);
//      Service service = Service.create(wsdlURL, serviceName);
//      return service.getPort(Endpoint.class);
   }

   public void performIteration(Object port) throws Exception
   {
      for (int i = 0; i < 100; i++) {
         String par = "Hello" + Math.random();
         String ret = ((Endpoint)port).echo(par);
         if (!(par.equals(ret)))
         {
            throw new Exception("Unexpected result: " + ret);
         }
      }
   }
}
