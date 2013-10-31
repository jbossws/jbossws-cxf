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
package org.jboss.test.ws.jaxws.complex;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * A complex JAX-WS test
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Thomas.Diesler@jboss.com
 */
public class ComplexTestCase extends JBossWSTest
{
   private Registration port;

   public static Test suite()
   {
      return new JBossWSTestSetup(ComplexTestCase.class, "jaxws-complex.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL =  getResourceURL("jaxws/complex/META-INF/wsdl/RegistrationService.wsdl");
      QName serviceName = new QName("http://complex.jaxws.ws.test.jboss.org/", "RegistrationService");
      Service service = Service.create(wsdlURL, serviceName);
      port = (Registration)service.getPort(Registration.class);
   }

   public void testRegistration() throws Exception
   {
      Customer customer = getFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());

      XMLGregorianCalendar cal = getCalendar();

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

   public void testInvoiceRegistration() throws Exception
   {
      InvoiceCustomer customer = getInvoiceFredJackson();
      customer.getReferredCustomers().add(getJohnDoe());
      customer.getReferredCustomers().add(getAlCapone());
      
      assertTrue(port.registerForInvoice(customer));
   }

   public void testOtherPackage() throws Exception
   {
      Statistics stats = port.getStatistics(getFredJackson());

      System.out.println(stats.getActivationTime());
      assertEquals(10, stats.getHits());
   }

   public void testBulkRegistration() throws Exception
   {
      List<Customer> customers = new ArrayList<Customer>();
      customers.add(getFredJackson());
      customers.add(getJohnDoe());

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
