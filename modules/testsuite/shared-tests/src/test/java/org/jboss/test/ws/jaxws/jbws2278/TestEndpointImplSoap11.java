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
package org.jboss.test.ws.jaxws.jbws2278;

import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.SOAPBinding;

/**
 * Test Endpoint implementation - SOAP 1.1 port.
 * 
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 */
@WebService(name = "TestEndpoint", portName="TestEndpointSoap11Port",  targetNamespace = "http://org.jboss.test.ws/jbws2278", serviceName="TestService", endpointInterface = "org.jboss.test.ws.jaxws.jbws2278.TestEndpoint", wsdlLocation="WEB-INF/wsdl/Test.wsdl")
@BindingType(SOAPBinding.SOAP11HTTP_BINDING)
public class TestEndpointImplSoap11 extends TestEndpointImpl implements TestEndpoint
{

}
