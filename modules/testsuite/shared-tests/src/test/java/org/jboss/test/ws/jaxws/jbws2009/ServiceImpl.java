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
package org.jboss.test.ws.jaxws.jbws2009;

import java.util.List;

import jakarta.jws.WebService;

import org.jboss.test.ws.jaxws.jbws2009.generated.CountryCodeType;
import org.jboss.test.ws.jaxws.jbws2009.generated.CurrencyCodeType;
import org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodesResponse;
import org.jboss.test.ws.jaxws.jbws2009.generated.ServiceType;
import org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodesResponse.Response;

@WebService(portName = "ServicePort", serviceName = "EndpointService", wsdlLocation = "WEB-INF/wsdl/TestService.wsdl", targetNamespace = "http://jbws2009.jaxws.ws.test.jboss.org/", endpointInterface = "org.jboss.test.ws.jaxws.jbws2009.generated.ServiceType")
public class ServiceImpl implements ServiceType
{

   public Response getCountryCodes()
   {
      Response response = new Response();
      List<CountryCodeType> values = response.getCountry();
      values.add(CountryCodeType.CZ);
      values.add(CountryCodeType.DE);
      return response;
   }

   public CurrencyCodeType getCurrency(CountryCodeType parameters)
   {
      if (parameters == CountryCodeType.CZ)
         return CurrencyCodeType.CZK;
      if (parameters == CountryCodeType.DE)
         return CurrencyCodeType.EUR;

      throw new IllegalArgumentException();
   }

   public GetCurrencyCodesResponse.Response getCurrencyCodes()
   {
      GetCurrencyCodesResponse.Response response = new GetCurrencyCodesResponse.Response();
      List<CurrencyCodeType> values = response.getCurrency();
      values.add(CurrencyCodeType.CZK);
      values.add(CurrencyCodeType.EUR);
      return response;
   }

}
