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
package org.jboss.test.ws.jaxws.jbws1843;

import java.util.List;

import jakarta.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1843.generated.CountryCodeType;
import org.jboss.test.ws.jaxws.jbws1843.generated.CurrencyCodeType;
import org.jboss.test.ws.jaxws.jbws1843.generated.GetCountryCodesResponse.Response;
import org.jboss.test.ws.jaxws.jbws1843.generated.Service;

/**
 * Test service implementation
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 10, 2007
 */
@WebService
(
      name="Service",
      portName="Service",
      serviceName="Service",   
      wsdlLocation="WEB-INF/wsdl/TestService.wsdl",
      targetNamespace = "http://jbws1843.jaxws.ws.test.jboss.org/",
      endpointInterface = "org.jboss.test.ws.jaxws.jbws1843.generated.Service"
)
public class ServiceImpl implements Service
{

   /*
    * @see org.jboss.test.ws.jaxws.jbws1843.generated.Service#getCountryCodes()
    */
   public Response getCountryCodes()
   {
      Response response = new Response();
      List<CountryCodeType> values = response.getCountry();
      values.add(CountryCodeType.CZ);
      values.add(CountryCodeType.DE);
      return response;
   }

   /*
    * @see org.jboss.test.ws.jaxws.jbws1843.generated.Service#getCurrency(org.jboss.test.ws.jaxws.jbws1843.generated.CountryCodeType)
    */
   public CurrencyCodeType getCurrency(CountryCodeType parameters)
   {
      if (parameters == CountryCodeType.CZ)
         return CurrencyCodeType.CZK;
      if (parameters == CountryCodeType.DE)
         return CurrencyCodeType.EUR;

      throw new IllegalArgumentException();
   }

   /*
    * @see org.jboss.test.ws.jaxws.jbws1843.generated.Service#getCurrencyCodes()
    */
   public org.jboss.test.ws.jaxws.jbws1843.generated.GetCurrencyCodesResponse.Response getCurrencyCodes()
   {
      org.jboss.test.ws.jaxws.jbws1843.generated.GetCurrencyCodesResponse.Response response =
         new org.jboss.test.ws.jaxws.jbws1843.generated.GetCurrencyCodesResponse.Response();
      List<CurrencyCodeType> values = response.getCurrency();
      values.add(CurrencyCodeType.CZK);
      values.add(CurrencyCodeType.EUR);
      return response;
   }

}
