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
package org.jboss.test.ws.jaxws.jbws2634.webservice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.jbws2634.shared.BeanIface;

/**
 * Basic endpoint implementation.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public abstract class AbstractEndpointImpl implements EndpointIface
{

   private static final Logger LOG = Logger.getLogger(AbstractEndpointImpl.class);

   private boolean correctState;
   @EJB
   private BeanIface testBean2;

   private BeanIface testBean1;

   @EJB(name = "jaxws-jbws2634/BeanImpl/local-org.jboss.test.ws.jaxws.jbws2634.shared.BeanIface")
   private void setBean(BeanIface bean)
   {
      this.testBean1 = bean;
   }

   private Boolean boolean1;

   /**
    * EJB 3.0 16.2.2: "By default, the name of the field is combined with the
    * name of the class in which the annotation is used and is used directly
    * as the name in the bean’s naming context
    */
   @Resource(name="boolean1")
   private void setBoolean1(Boolean b)
   {
      this.boolean1 = b;
   }

   public String echo(final String msg)
   {
      if (!this.correctState)
      {
         throw new WebServiceException("Injection failed");
      }

      LOG.info("echo: " + msg);
      return msg;
   }

   @PostConstruct
   private void init()
   {
      boolean currentState = true;

      if (this.testBean1 == null)
      {
         LOG.error("Annotation driven initialization for testBean1 failed");
         currentState = false;
      }
      if (!"Injected hello message".equals(testBean1.printString())) 
      {
         LOG.error("Annotation driven initialization for testBean1 failed");
         currentState = false;
      }
      if (this.testBean2 == null)
      {
         LOG.error("Annotation driven initialization for testBean2 failed");
         currentState = false;
      }
      if (!"Injected hello message".equals(testBean2.printString())) 
      {
         LOG.error("Annotation driven initialization for testBean2 failed");
         currentState = false;
      }
      if (this.boolean1 == null || this.boolean1 != true)
      {
         LOG.error("Annotation driven initialization for boolean1 failed");
         currentState = false;
      }

      this.correctState = currentState;
   }

}
