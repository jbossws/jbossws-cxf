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
package org.jboss.ws.bench;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Address;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Customer;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.InvoiceCustomer;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Name;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.PhoneNumber;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Registration;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.Statistics;
import org.jboss.test.ws.jaxws.benchmark.test.complex.types.ValidationFault_Exception;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTestHelper;

public class JAXWSBenchmarkComplex extends AbstractJavaSamplerClient
{
   private final String endpointURL = "http://" + JBossWSTestHelper.getServerHost() + ":" + JBossWSTestHelper.getServerPort() + "/jaxws-benchmark-complex/RegistrationServiceImpl";
   private final String targetNS = "http://complex.test.benchmark.jaxws.ws.test.jboss.org";
   private Registration ep;
   
   @Override
   public void setupTest(JavaSamplerContext context) {
      super.setupTest(context);
      try {
         this.ep = prepare();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public void teardownTest(JavaSamplerContext context) {
      super.teardownTest(context);
      this.ep = null;
   }
   
   @Override
   public SampleResult runTest(JavaSamplerContext ctx)
   {
      final SampleResult sampleResult = new SampleResult();
      sampleResult.sampleStart();

      try {
         performIteration(ep);
         sampleResult.setSuccessful(true);
      } catch (Exception e) {
         sampleResult.setSuccessful(false);
         sampleResult.setResponseMessage("Exception: " + e);
         StringWriter stringWriter = new StringWriter();
         e.printStackTrace(new PrintWriter(stringWriter));
         sampleResult.setResponseData(stringWriter.toString(), "UTF-8");
         sampleResult.setDataType(SampleResult.TEXT);
      } finally {
         sampleResult.sampleEnd();
      }

      return sampleResult;
   }

   public Registration prepare() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "RegistrationServiceImplService");

      //explicitly use JBossWS-CXF JAXWS SPI Provider impl as jmeter uses a flat classpath approach
      //and it's not possible to configure the jar order
      ProviderImpl p = new ProviderImpl();
      return p.createServiceDelegate(wsdlURL, serviceName, null).getPort(Registration.class);
//      Service service = Service.create(wsdlURL, serviceName);
//      return service.getPort(Registration.class);
   }

   public void performIteration(Object port) throws Exception
   {
      for (int i = 0; i < 10; i++) {
         int par = (int)(1000 * Math.random()) + 1;
         
         testRegistration((Registration)port, par);
         testInvoiceRegistration((Registration)port, par);
         testOtherPackage((Registration)port, par);
         testBulkRegistration((Registration)port, par);
      }
   }
   
   public void testRegistration(Registration port, int r) throws Exception
   {
      Customer customer = getFredJackson(r);
      customer.getReferredCustomers().add(getJohnDoe(r));
      customer.getReferredCustomers().add(getAlCapone(r));

      XMLGregorianCalendar cal = getCalendar();

      port.register(customer, cal);

      customer = getAlCapone(r);
      port.register(customer, cal);
   }

   public void testInvoiceRegistration(Registration port, int r) throws Exception
   {
      InvoiceCustomer customer = getInvoiceFredJackson(r);
      customer.getReferredCustomers().add(getJohnDoe(r));
      customer.getReferredCustomers().add(getAlCapone(r));

      port.registerForInvoice(customer);
   }

   public void testOtherPackage(Registration port, int r) throws Exception
   {
      Statistics stats = port.getStatistics(getFredJackson(r));

      stats.getActivationTime();
      stats.getHits();
   }

   public void testBulkRegistration(Registration port, int r) throws Exception
   {
      List<Customer> customers = new ArrayList<Customer>();
      customers.add(getFredJackson(r));
      customers.add(getJohnDoe(r));

      port.bulkRegister(customers, getCalendar());

      customers.clear();
      customers.add(getFredJackson(r));
      customers.add(getInvalid(754));
      customers.add(getInvalid(753));
      customers.add(getJohnDoe(r));
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

   private Customer getFredJackson(int r)
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
      customer.setId(r -1);
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.getContactNumbers().add(number2);
      return customer;
   }

   private InvoiceCustomer getInvoiceFredJackson(int r)
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
      customer.setId(r -1);
      customer.setName(name);
      customer.setAddress(address);
      customer.getContactNumbers().add(number1);
      customer.getContactNumbers().add(number2);
      customer.setCycleDay(10);
      return customer;
   }

   private Customer getJohnDoe(int r)
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
      customer.setId(r);
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

   private Customer getAlCapone(int r)
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
      customer.setId(r + 1);
      return customer;
   }

   private XMLGregorianCalendar getCalendar() throws DatatypeConfigurationException
   {
      DatatypeFactory calFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar cal = calFactory.newXMLGregorianCalendar(2002, 4, 5, 0, 0, 0, 0, 0);
      return cal;
   }
}
