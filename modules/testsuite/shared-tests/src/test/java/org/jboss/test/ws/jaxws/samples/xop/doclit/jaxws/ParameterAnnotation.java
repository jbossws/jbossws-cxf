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
package org.jboss.test.ws.jaxws.samples.xop.doclit.jaxws;

import jakarta.activation.DataHandler;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "parameterAnnotation", namespace = "http://doclit.xop.samples.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameterAnnotation", namespace = "http://doclit.xop.samples.jaxws.ws.test.jboss.org/")
public class ParameterAnnotation {

    @XmlElement(name = "arg0", namespace = "")
    @XmlMimeType("text/plain")
    private DataHandler arg0;

    /**
     * 
     * @return
     *     returns DataHandler
     */
    public DataHandler getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(DataHandler arg0) {
        this.arg0 = arg0;
    }

}
