/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
