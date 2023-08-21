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
package org.jboss.test.ws.jaxws.calendar;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.calendar package. 
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

    private final static QName _EchoXMLGregorianCalendar_QNAME = new QName("http://org.jboss.ws/jaxws/calendar", "echoXMLGregorianCalendar");
    private final static QName _EchoCalendar_QNAME = new QName("http://org.jboss.ws/jaxws/calendar", "echoCalendar");
    private final static QName _EchoCalendarResponse_QNAME = new QName("http://org.jboss.ws/jaxws/calendar", "echoCalendarResponse");
    private final static QName _EchoXMLGregorianCalendarResponse_QNAME = new QName("http://org.jboss.ws/jaxws/calendar", "echoXMLGregorianCalendarResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.calendar
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EchoXMLGregorianCalendar }
     * 
     */
    public EchoXMLGregorianCalendar createEchoXMLGregorianCalendar() {
        return new EchoXMLGregorianCalendar();
    }

    /**
     * Create an instance of {@link EchoCalendarResponse }
     * 
     */
    public EchoCalendarResponse createEchoCalendarResponse() {
        return new EchoCalendarResponse();
    }

    /**
     * Create an instance of {@link EchoXMLGregorianCalendarResponse }
     * 
     */
    public EchoXMLGregorianCalendarResponse createEchoXMLGregorianCalendarResponse() {
        return new EchoXMLGregorianCalendarResponse();
    }

    /**
     * Create an instance of {@link EchoCalendar }
     * 
     */
    public EchoCalendar createEchoCalendar() {
        return new EchoCalendar();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoXMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/jaxws/calendar", name = "echoXMLGregorianCalendar")
    public JAXBElement<EchoXMLGregorianCalendar> createEchoXMLGregorianCalendar(EchoXMLGregorianCalendar value) {
        return new JAXBElement<EchoXMLGregorianCalendar>(_EchoXMLGregorianCalendar_QNAME, EchoXMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/jaxws/calendar", name = "echoCalendar")
    public JAXBElement<EchoCalendar> createEchoCalendar(EchoCalendar value) {
        return new JAXBElement<EchoCalendar>(_EchoCalendar_QNAME, EchoCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoCalendarResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/jaxws/calendar", name = "echoCalendarResponse")
    public JAXBElement<EchoCalendarResponse> createEchoCalendarResponse(EchoCalendarResponse value) {
        return new JAXBElement<EchoCalendarResponse>(_EchoCalendarResponse_QNAME, EchoCalendarResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoXMLGregorianCalendarResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/jaxws/calendar", name = "echoXMLGregorianCalendarResponse")
    public JAXBElement<EchoXMLGregorianCalendarResponse> createEchoXMLGregorianCalendarResponse(EchoXMLGregorianCalendarResponse value) {
        return new JAXBElement<EchoXMLGregorianCalendarResponse>(_EchoXMLGregorianCalendarResponse_QNAME, EchoXMLGregorianCalendarResponse.class, null, value);
    }

}
