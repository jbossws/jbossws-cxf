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
package org.jboss.test.ws.jaxws.wrapped.accessor.jaxws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "methodAccessorResponse", namespace = "http://accessor.wrapped.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "methodAccessorResponse", namespace = "http://accessor.wrapped.jaxws.ws.test.jboss.org/")
public class MethodAccessorResponse {

    private String renamed;

    /**
     *
     * @return
     *     returns String
     */
    @XmlElement(name = "return", namespace = "")
    public String get_return() {
        return this.renamed;
    }

    /**
     *
     * @param _return
     *     the value for the _return property
     */
    public void set_return(String _return) {
        this.renamed = _return;
    }
}
