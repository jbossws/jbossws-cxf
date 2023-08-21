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
package org.jboss.test.ws.jaxws.samples.soapbinding;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Test the JSR-181 annotation: jakarta.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 16-Oct-2005
 */
@WebService(name = "DocBare", serviceName = "DocBareService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface DocBare
{
   @WebMethod(operationName = "SubmitPO")
   SubmitBareResponse submitPO(SubmitBareRequest poRequest);

   @WebMethod(operationName = "SubmitNamespacedPO")
   @WebResult(targetNamespace = "http://namespace/result", name = "SubmitBareResponse")
   SubmitBareResponse submitNamespacedPO(@WebParam(targetNamespace = "http://namespace/request") SubmitBareRequest poRequest);
}
