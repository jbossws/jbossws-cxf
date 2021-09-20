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
