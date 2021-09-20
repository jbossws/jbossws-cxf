/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.benchmark.test.complex;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.test.ws.jaxws.benchmark.BenchmarkTest;
import org.jboss.test.ws.jaxws.benchmark.Runner;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Address;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Customer;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.InvoiceCustomer;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Name;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.PhoneNumber;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Registration;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Statistics;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.ValidationFault_Exception;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Sep-2009
 *
 */
public class ComplexTest implements BenchmarkTest
{
   private final String endpointURL = "http://" + Runner.getServerAddress() + "/jaxws-benchmark-complex/RegistrationServiceImpl";
   private final String targetNS = "http://complex.test.benchmark.jaxws.ws.test.jboss.org";

   @Override
   public Object prepare() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "RegistrationServiceImplService");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Registration.class);
   }

   @Override
   public void performIteration(Object port) throws Exception
   {
      testRegistration((Registration)port);
      testInvoiceRegistration((Registration)port);
      testOtherPackage((Registration)port);
      testBulkRegistration((Registration)port);
   }

   public void testRegistration(Registration port) throws Exception
   {
      Customer customer = getFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());

      XMLGregorianCalendar cal = getCalendar();

      port.register(customer, cal);

      customer = getAlCapone();
      port.register(customer, cal);
   }

   public void testInvoiceRegistration(Registration port) throws Exception
   {
      InvoiceCustomer customer = getInvoiceFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());

      port.registerForInvoice(customer);
   }

   public void testOtherPackage(Registration port) throws Exception
   {
      Statistics stats = port.getStatistics(getFredJackson());

      stats.getActivationTime();
      stats.getHits();
   }

   public void testBulkRegistration(Registration port) throws Exception
   {
      List<Customer> customers = new ArrayList<Customer>();
      customers.add(getFredJackson());
      customers.add(getJohnDoe());

      port.bulkRegister(customers, getCalendar());

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
         //OK
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
