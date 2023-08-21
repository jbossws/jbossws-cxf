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

/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Thu Nov 02 21:17:37 CET 2006
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jaxws.jbws1581;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

@WebService (name = "EndpointBean")
@SOAPBinding(style = SOAPBinding.Style.RPC)

/**
 * The SEI that is packaged with the war endpoint may potentially
 * contain different annotations that the one packaged with the EJB client
 */
public interface EndpointInterface
{
   String hello(String msg);
}
