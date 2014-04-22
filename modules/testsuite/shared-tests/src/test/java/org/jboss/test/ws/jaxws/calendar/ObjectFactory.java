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
package org.jboss.test.ws.jaxws.calendar;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
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
