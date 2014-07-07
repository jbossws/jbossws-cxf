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
package org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.example.schema.doubleit package. 
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

    private final static QName _DoubleItHeader_QNAME = new QName("http://www.example.org/schema/DoubleIt", "DoubleItHeader");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.example.schema.doubleit
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DoubleItFault }
     * 
     */
    public DoubleItFault createDoubleItFault() {
        return new DoubleItFault();
    }

    /**
     * Create an instance of {@link DoubleItResponse }
     * 
     */
    public DoubleItResponse createDoubleItResponse() {
        return new DoubleItResponse();
    }

    /**
     * Create an instance of {@link DoubleIt2 }
     * 
     */
    public DoubleIt2 createDoubleIt2() {
        return new DoubleIt2();
    }

    /**
     * Create an instance of {@link DoubleIt }
     * 
     */
    public DoubleIt createDoubleIt() {
        return new DoubleIt();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/schema/DoubleIt", name = "DoubleItHeader")
    public JAXBElement<Integer> createDoubleItHeader(Integer value) {
        return new JAXBElement<Integer>(_DoubleItHeader_QNAME, Integer.class, null, value);
    }

}
