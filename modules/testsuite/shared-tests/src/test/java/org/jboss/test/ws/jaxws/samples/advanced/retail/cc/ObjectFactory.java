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
package org.jboss.test.ws.jaxws.samples.advanced.retail.cc;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.samples.advanced.retail.cc package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _VerifyResponse_QNAME = new QName("http://org.jboss.ws/samples/retail/cc", "verifyResponse");
    private final static QName _Verify_QNAME = new QName("http://org.jboss.ws/samples/retail/cc", "verify");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.samples.advanced.retail.cc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link VerificationRequest }
     * 
     */
    public VerificationRequest createVerificationRequest() {
        return new VerificationRequest();
    }

    /**
     * Create an instance of {@link VerificationResponse }
     * 
     */
    public VerificationResponse createVerificationResponse() {
        return new VerificationResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/cc", name = "verifyResponse")
    public JAXBElement<VerificationResponse> createVerifyResponse(VerificationResponse value) {
        return new JAXBElement<VerificationResponse>(_VerifyResponse_QNAME, VerificationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificationRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/cc", name = "verify")
    public JAXBElement<VerificationRequest> createVerify(VerificationRequest value) {
        return new JAXBElement<VerificationRequest>(_Verify_QNAME, VerificationRequest.class, null, value);
    }

}
