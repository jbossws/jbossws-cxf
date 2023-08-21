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
package org.jboss.test.ws.jaxws.jbws1798;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws1798.generated.CountryCodeType;
import org.jboss.test.ws.jaxws.jbws1798.generated.CurrencyCodeType;
import org.jboss.test.ws.jaxws.jbws1798.generated.GetCountryCodesResponse.Response;
import org.jboss.test.ws.jaxws.jbws1798.generated.ServiceType;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1798] JBossWS cannot find local schema with relative urls
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2007
 */
@RunWith(Arquillian.class)
public class JBWS1798TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1798.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1798.ServiceImpl.class)
               .addPackage("org.jboss.test.ws.jaxws.jbws1798.generated")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1798/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1798/WEB-INF/wsdl/common/1.0-SNAPSHOT/CoreComponentTypes.xsd"), "wsdl/common/1.0-SNAPSHOT/CoreComponentTypes.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1798/WEB-INF/wsdl/imported/my-service/1.0-SNAPSHOT/BaseComponents.xsd"), "wsdl/imported/my-service/1.0-SNAPSHOT/BaseComponents.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1798/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCountryCodesAndCurrencies() throws Exception
   {
      QName serviceName = new QName("http://jbws1798.jaxws.ws.test.jboss.org/", "EndpointService");
      URL wsdlURL = new URL(baseURL + "/Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      ServiceType proxy = service.getPort(ServiceType.class);
      
      Response response = proxy.getCountryCodes();
      List<CountryCodeType> countryCodes = response.getCountry();
      assertEquals(countryCodes.get(0), CountryCodeType.CZ);
      assertEquals(countryCodes.get(1), CountryCodeType.DE);
      
      org.jboss.test.ws.jaxws.jbws1798.generated.GetCurrencyCodesResponse.Response response2 = proxy.getCurrencyCodes();
      List<CurrencyCodeType> currencyCodes = response2.getCurrency();
      assertEquals(currencyCodes.get(0), CurrencyCodeType.CZK);
      assertEquals(currencyCodes.get(1), CurrencyCodeType.EUR);
      
      assertEquals(CurrencyCodeType.CZK, proxy.getCurrency(CountryCodeType.CZ));
      assertEquals(CurrencyCodeType.EUR, proxy.getCurrency(CountryCodeType.DE));
   }
}
