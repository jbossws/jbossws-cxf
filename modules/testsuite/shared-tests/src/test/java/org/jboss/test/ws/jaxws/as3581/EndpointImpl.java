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

import jakarta.jws.Oneway;
import jakarta.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService(
   endpointInterface = "org.jboss.test.ws.jaxws.as3581.EndpointIface",
   targetNamespace = "org.jboss.test.ws.jaxws.as3581",
   serviceName = "SimpleService"
)
public class EndpointImpl
{

    static volatile String value;
    static volatile RuntimeException ex;

    @Oneway
    public void doit()
    {
        try
        {
            value = (String)(new InitialContext().lookup("java:comp/env/message"));
            if (!"Ahoj".equals(value))
            {
                ex = new RuntimeException("JNDI lookup in @Oneway method failed");
            }
        }
        catch (final NamingException e)
        {
            ex = new RuntimeException("JNDI lookup in @Oneway method failed", e);
        }
    }

}
