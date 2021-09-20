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
