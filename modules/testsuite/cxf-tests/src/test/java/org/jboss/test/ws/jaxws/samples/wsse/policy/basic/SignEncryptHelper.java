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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.wsf.test.ClientHelper;
import org.jboss.wsf.test.CryptoCheckHelper;

public class SignEncryptHelper implements ClientHelper
{
   private String targetEndpoint;
   
   @Override
   public void setTargetEndpoint(String address)
   {
      targetEndpoint = address;
   }
   
   public boolean testSignEncrypt() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         ServiceIface proxy = getProxy();
         setupWsse(proxy);
         return invoke(proxy);
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public boolean testSignEncryptUsingConfigProperties() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         ServiceIface proxy = getProxy();
         ClientConfigUtil.setConfigProperties(proxy, "META-INF/jaxws-client-config.xml", "Custom WS-Security Client");
         return invoke(proxy);
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   private ServiceIface getProxy() throws MalformedURLException {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(targetEndpoint + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      return (ServiceIface)service.getPort(ServiceIface.class);
   }
   
   private boolean invoke(ServiceIface proxy) throws Exception {
      try
      {
         return "Secure Hello World!".equals(proxy.sayHello());
      }
      catch (SOAPFaultException e)
      {
         throw CryptoCheckHelper.checkAndWrapException(e);
      }
   }
   
   private void setupWsse(ServiceIface proxy)
   {
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new KeystorePasswordCallback());
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, Thread.currentThread().getContextClassLoader().getResource("META-INF/alice.properties"));
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "alice");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.ENCRYPT_USERNAME, "bob");
   }
}
