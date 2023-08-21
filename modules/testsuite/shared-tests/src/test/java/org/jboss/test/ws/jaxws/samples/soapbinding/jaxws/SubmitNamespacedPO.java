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
package org.jboss.test.ws.jaxws.samples.soapbinding.jaxws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "SubmitNamespacedPO", namespace = "http://soapbinding.samples.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubmitNamespacedPO", namespace = "http://soapbinding.samples.jaxws.ws.test.jboss.org/", propOrder = {
    "namespacedPurchaseOrder",
    "namespacedString"
})
public class SubmitNamespacedPO {

    @XmlElement(name = "NamespacedPurchaseOrder", namespace = "http://namespace/purchase")
    private String namespacedPurchaseOrder;
    @XmlElement(name = "NamespacedString", namespace = "http://namespace/string")
    private String namespacedString;

    /**
     * 
     * @return
     *     returns String
     */
    public String getNamespacedPurchaseOrder() {
        return this.namespacedPurchaseOrder;
    }

    /**
     * 
     * @param namespacedPurchaseOrder
     *     the value for the namespacedPurchaseOrder property
     */
    public void setNamespacedPurchaseOrder(String namespacedPurchaseOrder) {
        this.namespacedPurchaseOrder = namespacedPurchaseOrder;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getNamespacedString() {
        return this.namespacedString;
    }

    /**
     * 
     * @param namespacedString
     *     the value for the namespacedString property
     */
    public void setNamespacedString(String namespacedString) {
        this.namespacedString = namespacedString;
    }

}
