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
package org.jboss.test.ws.jaxws.anonymous;

import jakarta.jws.WebService;

/**
 * An endpoint that echos an anonymous type.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@WebService(endpointInterface="org.jboss.test.ws.jaxws.anonymous.Anonymous", serviceName="AnonymousService")
public class AnonymousImpl implements Anonymous
{
   public AnonymousResponse echoAnonymous(AnonymousRequest request)
   {
      AnonymousResponse response = new AnonymousResponse();
      response.message = request.message;

      return response;
   }
}
