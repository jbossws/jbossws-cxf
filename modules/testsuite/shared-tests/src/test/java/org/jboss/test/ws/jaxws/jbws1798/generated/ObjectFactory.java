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
package org.jboss.test.ws.jaxws.jbws1798.generated;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.jbws1798.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetCurrencyResponse_QNAME = new QName("http://jbws1798.jaxws.ws.test.jboss.org/", "getCurrencyResponse");
    private final static QName _GetCurrency_QNAME = new QName("http://jbws1798.jaxws.ws.test.jboss.org/", "getCurrency");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.jbws1798.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCountryCodesResponse.Response }
     * 
     */
    public GetCountryCodesResponse.Response createGetCountryCodesResponseResponse() {
        return new GetCountryCodesResponse.Response();
    }

    /**
     * Create an instance of {@link GetCurrencyCodesResponse.Response }
     * 
     */
    public GetCurrencyCodesResponse.Response createGetCurrencyCodesResponseResponse() {
        return new GetCurrencyCodesResponse.Response();
    }

    /**
     * Create an instance of {@link GetCountryCodes }
     * 
     */
    public GetCountryCodes createGetCountryCodes() {
        return new GetCountryCodes();
    }

    /**
     * Create an instance of {@link GetCurrencyCodesResponse }
     * 
     */
    public GetCurrencyCodesResponse createGetCurrencyCodesResponse() {
        return new GetCurrencyCodesResponse();
    }

    /**
     * Create an instance of {@link GetCurrencyCodes }
     * 
     */
    public GetCurrencyCodes createGetCurrencyCodes() {
        return new GetCurrencyCodes();
    }

    /**
     * Create an instance of {@link GetCountryCodesResponse }
     * 
     */
    public GetCountryCodesResponse createGetCountryCodesResponse() {
        return new GetCountryCodesResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CurrencyCodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://jbws1798.jaxws.ws.test.jboss.org/", name = "getCurrencyResponse")
    public JAXBElement<CurrencyCodeType> createGetCurrencyResponse(CurrencyCodeType value) {
        return new JAXBElement<CurrencyCodeType>(_GetCurrencyResponse_QNAME, CurrencyCodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CountryCodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://jbws1798.jaxws.ws.test.jboss.org/", name = "getCurrency")
    public JAXBElement<CountryCodeType> createGetCurrency(CountryCodeType value) {
        return new JAXBElement<CountryCodeType>(_GetCurrency_QNAME, CountryCodeType.class, null, value);
    }

}
