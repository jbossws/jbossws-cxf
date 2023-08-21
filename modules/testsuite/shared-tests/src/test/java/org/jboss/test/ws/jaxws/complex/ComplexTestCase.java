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
package org.jboss.test.ws.jaxws.complex;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A complex JAX-WS test
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class ComplexTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-complex.war");
         archive
            .addManifest()
            .addPackages(false, new Filter<ArchivePath>() {
               @Override
               public boolean include(ArchivePath path)
               {
                  return !path.get().contains("TestCase");
               }
            }, "org.jboss.test.ws.jaxws.complex")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/complex/WEB-INF/web.xml"));
      return archive;
   }

   private Registration getPort() {
      Registration port = null;
      try {
         URL wsdlURL = JBossWSTestHelper.getResourceURL("jaxws/complex/META-INF/wsdl/RegistrationService.wsdl");
         QName serviceName = new QName("http://complex.jaxws.ws.test.jboss.org/", "RegistrationService");
         Service service = Service.create(wsdlURL, serviceName);
         port = (Registration) service.getPort(Registration.class);
         ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL + "/RegistrationService");
      } catch (Exception e) {
         System.out.println(e);
      }
      return port;
   }

   @Test
   @RunAsClient
   public void testRegistration() throws Exception
   {
      Customer customer = getFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());

      XMLGregorianCalendar cal = getCalendar();
      Registration port = getPort();
      port.register(customer, cal);

      customer = getAlCapone();
      try
      {
         port.register(customer, cal);
         fail("Expected AlreadyRegisteredFault");
      }
      catch (AlreadyRegisteredFault_Exception e)
      {
         assertEquals(456, e.getFaultInfo().getExistingId());
      }
   }

   @Test
   @RunAsClient
   public void testInvoiceRegistration() throws Exception
   {
      InvoiceCustomer customer = getInvoiceFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());
      Registration port = getPort();
      assertTrue(port.registerForInvoice(customer));
   }

   @Test
   @RunAsClient
   public void testOtherPackage() throws Exception
   {
      Registration port = getPort();
      Statistics stats = port.getStatistics(getFredJackson());

      System.out.println(stats.getActivationTime());
      assertEquals(10, stats.getHits());
   }

   @Test
   @RunAsClient
   public void testBulkRegistration() throws Exception
   {
      List<Customer> customers = new ArrayList<Customer>();
      customers.add(getFredJackson());
      customers.add(getJohnDoe());

      Registration port = getPort();
      List<Long> result = port.bulkRegister(customers, getCalendar());

      assertEquals(123, result.get(0).longValue());
      assertEquals(124, result.get(1).longValue());

      customers.clear();
      customers.add(getFredJackson());
      customers.add(getInvalid(754));
      customers.add(getInvalid(753));
      customers.add(getJohnDoe());
      customers.add(getInvalid(752));

      try
      {
         port.bulkRegister(customers, getCalendar());
      }
      catch (ValidationFault_Exception e)
      {
         List<Long> failedCustomers = e.getFaultInfo().getFailedCustomers();
         assertEquals(754, failedCustomers.get(0).longValue());
         assertEquals(753, failedCustomers.get(1).longValue());
         assertEquals(752, failedCustomers.get(2).longValue());
      }
   }

   private Customer getFredJackson()
   {
      Name name = new Name();
      name.setFirstName("Fred");
      name.setMiddleName("Jones");
      name.setLastName("Jackson");

      Address address = new Address();
      address.setCity("Atlanta");
      address.setState("Georgia");
      address.setZip("53717");
      address.setStreet("Yet Another Peach Tree St.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("123");
      number1.setExchange("456");
      number1.setLine("7890");

      PhoneNumber number2 = new PhoneNumber();
      number1.setAreaCode("333");
      number1.setExchange("222");
      number1.setLine("1234");

      Customer customer = new Customer();
      customer.setId(123);
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.getContactNumbers().add(number2);
      return customer;
   }

   private InvoiceCustomer getInvoiceFredJackson()
   {
      Name name = new Name();
      name.setFirstName("Fred");
      name.setMiddleName("Jones");
      name.setLastName("Jackson");

      Address address = new Address();
      address.setCity("Atlanta");
      address.setState("Georgia");
      address.setZip("53717");
      address.setStreet("Yet Another Peach Tree St.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("123");
      number1.setExchange("456");
      number1.setLine("7890");

      PhoneNumber number2 = new PhoneNumber();
      number1.setAreaCode("333");
      number1.setExchange("222");
      number1.setLine("1234");

      InvoiceCustomer customer = new InvoiceCustomer();
      customer.setId(123);
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.getContactNumbers().add(number2);
      customer.setCycleDay(10);
      return customer;
   }

   private Customer getJohnDoe()
   {
      Name name = new Name();
      name.setFirstName("John");
      name.setLastName("Doe");

      Address address = new Address();
      address.setCity("New York");
      address.setState("New York");
      address.setZip("10010");
      address.setStreet("Park Street");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("555");
      number1.setExchange("867");
      number1.setLine("5309");

      Customer customer = new Customer();
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.setId(124);
      return customer;
   }

   private Customer getInvalid(long id)
   {
      Address address = new Address();
      address.setCity("New York");
      address.setState("New York");
      address.setZip("10010");
      address.setStreet("Park Street");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("555");
      number1.setExchange("867");
      number1.setLine("5309");

      Customer customer = new Customer();
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.setId(id);
      return customer;
   }

   private Customer getAlCapone()
   {
      Name name = new Name();
      name.setFirstName("Al");
      name.setLastName("Capone");

      Address address = new Address();
      address.setCity("Chicago");
      address.setState("Illinois");
      address.setZip("60619");
      address.setStreet("7244 South Prairie Avenue.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("888");
      number1.setExchange("722");
      number1.setLine("7322");

      Customer customer = new Customer();
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.setId(125);
      return customer;
   }

   private XMLGregorianCalendar getCalendar() throws DatatypeConfigurationException
   {
      DatatypeFactory calFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar cal = calFactory.newXMLGregorianCalendar(2002, 4, 5, 0, 0, 0, 0, 0);
      return cal;
   }
}
