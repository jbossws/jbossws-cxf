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

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


@XmlRegistry
public class ObjectFactory {

    private final static QName _TestException_QNAME = new QName("http://org.jboss.test.ws/jbws2278/types", "TestException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.jbws2278
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TestException }
     * 
     */
    public TestException createTestException() {
        return new TestException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.test.ws/jbws2278/types", name = "TestException")
    public JAXBElement<TestException> createTestException(TestException value) {
        return new JAXBElement<TestException>(_TestException_QNAME, TestException.class, null, value);
    }

}
