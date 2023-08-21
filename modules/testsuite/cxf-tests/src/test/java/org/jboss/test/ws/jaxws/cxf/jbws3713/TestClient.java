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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.net.URL;

public class TestClient
{
   public static void main(String[] args) throws Exception
   {
      final String wsdlAddress = args[0];
      final String threadPoolSize = args[1];
      final String invocations = args[2];
      int ret1 = new HelperUsignThreadLocal().run(new URL(wsdlAddress), Integer.parseInt(threadPoolSize), Integer.parseInt(invocations));
      int ret2 = new Helper().run(new URL(wsdlAddress), Integer.parseInt(threadPoolSize), Integer.parseInt(invocations));
      System.out.println(String.valueOf(ret1) + " " + String.valueOf(ret2));
      
      //wait a bit before returning as the log processing can be aysnch, the test client
      //relies on the log contents and the log streams are closed by the system when the
      //process terminates
      Thread.sleep(1000);
   }
}
