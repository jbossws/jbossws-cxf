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

package org.jboss.test.ws.jaxws.as3581;

import jakarta.jws.WebService;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService(
   endpointInterface = "org.jboss.test.ws.jaxws.as3581.EndpointIface2",
   targetNamespace = "org.jboss.test.ws.jaxws.as3581",
   serviceName = "SimpleService2"
)
public class EndpointImpl2
{

    public String getString()
    {
        for (int i = 0; i < 30; i++)
        {
           if (EndpointImpl.ex != null) throw EndpointImpl.ex;
           if (EndpointImpl.value != null) return EndpointImpl.value;
           try
           {
               Thread.sleep(1000);
           }
           catch (InterruptedException e)
           {
               throw new RuntimeException(e);
           }
        }
        throw new RuntimeException("Timeout: Cannot get injected value");
    }

}
