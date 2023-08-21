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
package org.jboss.test.ws.jaxws.webfault;

import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.ws.WebFault;

/**
 * A custom exception
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Feb-2008
 */
@WebFault(name = "myCustomFault", targetNamespace= "org.jboss.test.ws.jaxws.webfault.exceptions")
public class CustomException extends Exception
{
   private static final long serialVersionUID = 5054777794472486392L;
   private Integer number;
   @XmlTransient
   private String transientString;

   public CustomException()
   {
      super();
   }

   public CustomException(String message, Integer number)
   {
      super(message);
      this.number = number;
   }

   public CustomException(String message, Integer number, String transientString)
   {
      super(message);
      this.number = number;
      this.transientString = transientString;
   }

   public Integer getNumber()
   {
      return number;
   }

   public void setNumber(Integer number)
   {
      this.number = number;
   }

   @XmlTransient
   public String getTransientString()
   {
      return transientString;
   }

   public void setTransientString(String transientString)
   {
      this.transientString = transientString;
   }
}
