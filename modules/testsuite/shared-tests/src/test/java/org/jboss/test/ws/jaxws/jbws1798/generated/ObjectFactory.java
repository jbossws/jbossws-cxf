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
