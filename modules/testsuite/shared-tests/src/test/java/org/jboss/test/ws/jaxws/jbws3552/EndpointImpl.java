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
package org.jboss.test.ws.jaxws.jbws3552;

import jakarta.jws.WebService;

@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws3552.EndpointIface")
public class EndpointImpl {
    public AdaptedObjectCA echoCA(AdaptedObjectCA ao) {
        return ao;
    }

    public AbstractObjectCA echoAbstractCA(AbstractObjectCA ao) {
        return ao;
    }

    public AdaptedObjectFA echoFA(AdaptedObjectFA ao) {
        return ao;
    }

    public AbstractObjectFA echoAbstractFA(AbstractObjectFA ao) {
        return ao;
    }

    public AdaptedObjectGA echoGA(AdaptedObjectGA ao) {
        return ao;
    }

    public AbstractObjectGA echoAbstractGA(AbstractObjectGA ao) {
        return ao;
    }

    public AdaptedObjectMA echoMA(AdaptedObjectMA ao) {
        return ao;
    }

    public AbstractObjectMA echoAbstractMA(AbstractObjectMA ao) {
        return ao;
    }

    public void throwExceptionCA() throws AdaptedExceptionCA {
        throw new ExtendedAdaptedExceptionCA(666, "exception message", "exception description", new ComplexObjectCA("c", "d"));
    }

    public void throwExtendedExceptionCA() throws ExtendedAdaptedExceptionCA {
        throw new ExtendedAdaptedExceptionCA(666, "exception message", "exception description", new ComplexObjectCA("c", "d"));
    }

    public void throwExceptionFA() throws AdaptedExceptionFA {
        throw new ExtendedAdaptedExceptionFA(666, "exception message", "exception description", new ComplexObjectFA("c", "d"));
    }

    public void throwExtendedExceptionFA() throws ExtendedAdaptedExceptionFA {
        throw new ExtendedAdaptedExceptionFA(666, "exception message", "exception description", new ComplexObjectFA("c", "d"));
    }

    public void throwExceptionGA() throws AdaptedExceptionGA {
        throw new ExtendedAdaptedExceptionGA(666, "exception message", "exception description", new ComplexObjectGA("c", "d"));
    }

    public void throwExtendedExceptionGA() throws ExtendedAdaptedExceptionGA {
        throw new ExtendedAdaptedExceptionGA(666, "exception message", "exception description", new ComplexObjectGA("c", "d"));
    }

    public void throwExceptionMA() throws AdaptedExceptionMA {
        throw new ExtendedAdaptedExceptionMA(666, "exception message", "exception description", new ComplexObjectMA("c", "d"));
    }

    public void throwExtendedExceptionMA() throws ExtendedAdaptedExceptionMA {
        throw new ExtendedAdaptedExceptionMA(666, "exception message", "exception description", new ComplexObjectMA("c", "d"));
    }
}
