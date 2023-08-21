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

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws1843.generated.CountryCodeType;
import org.jboss.test.ws.jaxws.jbws1843.generated.CurrencyCodeType;
import org.jboss.test.ws.jaxws.jbws1843.generated.GetCountryCodesResponse.Response;
import org.jboss.test.ws.jaxws.jbws1843.generated.Service;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1843] WSDL with custom schema imports causes 
 * <b>java.lang.OutOfMemoryError: Java heap space</b>
 * when there are circular schema imports dependencies
 *
 * @author richard.opalka@jboss.com
 * @since Oct 10, 2007
 */
@RunWith(Arquillian.class)
public class JBWS1843TestCase extends JBossWSTest
{
   private static Service proxy;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1843.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1843.ServiceImpl.class)
               .addPackage("org.jboss.test.ws.jaxws.jbws1843.generated")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1843/WEB-INF/wsdl/BaseComponents.xsd"), "wsdl/BaseComponents.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1843/WEB-INF/wsdl/CoreComponentTypes.xsd"), "wsdl/CoreComponentTypes.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1843/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1843/WEB-INF/web.xml"));
      return archive;
   }

   @BeforeClass
   public static void setup() throws Exception
   {
      QName serviceName = new QName("http://jbws1843.jaxws.ws.test.jboss.org/", "Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws1843/Service?wsdl");
   
      jakarta.xml.ws.Service service = jakarta.xml.ws.Service.create(wsdlURL, serviceName);
      proxy = (Service)service.getPort(Service.class);
   }
   
   @AfterClass
   public static void cleanup() {
      proxy = null;
   }

   @Test
   @RunAsClient
   public void testCountryCodes() throws Exception
   {
      Response response = proxy.getCountryCodes();
      List<CountryCodeType> countryCodes = response.getCountry();
      assertEquals(countryCodes.get(0), CountryCodeType.CZ);
      assertEquals(countryCodes.get(1), CountryCodeType.DE);
   }

   @Test
   @RunAsClient
   public void testCurrencyCodes() throws Exception
   {
      org.jboss.test.ws.jaxws.jbws1843.generated.GetCurrencyCodesResponse.Response response = proxy.getCurrencyCodes();
      List<CurrencyCodeType> currencyCodes = response.getCurrency();
      assertEquals(currencyCodes.get(0), CurrencyCodeType.CZK);
      assertEquals(currencyCodes.get(1), CurrencyCodeType.EUR);
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      assertEquals(CurrencyCodeType.CZK, proxy.getCurrency(CountryCodeType.CZ));
      assertEquals(CurrencyCodeType.EUR, proxy.getCurrency(CountryCodeType.DE));
   }

}
