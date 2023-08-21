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
package org.jboss.test.ws.jaxws.samples.advanced.retail;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.samples.advanced.retail.cc.CCVerification;
import org.jboss.test.ws.jaxws.samples.advanced.retail.cc.CCVerificationService;
import org.jboss.test.ws.jaxws.samples.advanced.retail.profile.DiscountRequest;
import org.jboss.test.ws.jaxws.samples.advanced.retail.profile.DiscountResponse;
import org.jboss.test.ws.jaxws.samples.advanced.retail.profile.ProfileMgmt;
import org.jboss.test.ws.jaxws.samples.advanced.retail.profile.ProfileMgmtService;
import org.jboss.ws.api.annotation.WebContext;

/**
 * An example order management component
 * that offers access though RMI and SOAP
 */
@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.samples.advanced.retail.OrderMgmt", serviceName = "OrderMgmtService")
@WebContext(contextRoot = "/jaxws-samples-retail")
@HandlerChain(file = "jaxws-handler.xml")
public class OrderMgmtBean implements OrderMgmt
{

   private static final Logger log = Logger.getLogger(OrderMgmtBean.class);

   @WebServiceRef(wsdlLocation = "META-INF/wsdl/CCVerificationService.wsdl")
   private CCVerificationService verificationService;
   @SuppressWarnings("unused")
   private CCVerification verificationPort;

   @WebServiceRef(wsdlLocation = "META-INF/wsdl/ProfileMgmtService.wsdl")
   private ProfileMgmtService profileService;
   @SuppressWarnings("unused")
   private ProfileMgmt profilePort;

   @PostConstruct
   public void initialize()
   {
      // Throws NPE with SUN-RI, use lazy initialize instead
      //verificationPort = verificationService.getCCVerificationPort();
      //profilePort = profileService.getProfileMgmtPort();
   }

   public CCVerification getVerificationPort()
   {
      return verificationService.getCCVerificationPort();
   }

   public ProfileMgmt getProfilePort()
   {
      return profileService.getProfileMgmtPort();
   }

   /**
    * Prepare a customer order.
    * This will verify the billing details (i.e. creditcard)
    * and check if the customer qualifies for a discount
    * (applies to high value customers only)
    *
    * @param order
    * @return OrderStaus
    */
   @Override
   public OrderStatus prepareOrder(Order order)
   {

      log.info("Preparing order " + order);

      // verify billing details
      String creditCard = order.getCustomer().getCreditCardDetails();
      //Response<Boolean> response = getVerificationPort().verifyAsync(creditCard);

      boolean validCard = getVerificationPort().verify(creditCard);

      // high value customer discount
      DiscountRequest discountRequest = new DiscountRequest(order.getCustomer());
      DiscountResponse discount = getProfilePort().getCustomerDiscount(discountRequest);
      boolean hasDiscount = discount.getDiscount() > 0.00;
      log.info("High value customer ? " + hasDiscount);

      try
      {
         //log.info(creditCard + " valid? " + response.get());
         log.info(creditCard + " valid? " + validCard);
      }
      catch (Exception e)
      {
         log.error("Failed to access async results", e);
      }

      // transition to prepared state
      order.setState(Order.OrderState.PREPARED);

      // done
      //return new OrderStatus("Prepared", order.getOrderNum(), discount.getDiscount());
      return new OrderStatus("Prepared", order.getOrderNum(), 0);
   }

}
