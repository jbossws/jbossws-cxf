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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

import org.apache.cxf.transport.http.HTTPException;
import org.jboss.test.ws.jaxws.cxf.httpauth.Hello;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import java.net.URL;

/**
 * Class with main method used by {@link org.jboss.test.ws.jaxws.cxf.clientConfig.CXFClientConfigElytronNotOnClasspathTestCase}
 *
 * @author dvilkola@redhat.com
 * @since August-2019
 */
public class CXFClientElytronNotOnClasspathMainClass {

   public static void main(String[] args) throws Exception {
      QName serviceName = new QName("http://jboss.org/http/security", "HelloService");
      URL wsdlURL = CXFClientElytronNotOnClasspathMainClass.class.getResource("META-INF/wsdl/hello.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Hello proxy = service.getPort(Hello.class);
      BindingProvider bp = (BindingProvider) proxy;
      CXFClientConfigurer cxfClientConfigurer = new CXFClientConfigurer();
      ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/jaxws-samples-wsse/HelloService");
      // set properties from config file and check that elytron config file is ignored since elytron is not on classpath
      cxfClientConfigurer.setConfigProperties(proxy, "META-INF/jaxws-client-config.xml", "Custom Client Config");
      int status = 200;
      try {
         // request will fail without credentials
         proxy.helloRequest("number");
      } catch (WebServiceException e) {
         Throwable cause = e.getCause();
         if (cause instanceof HTTPException) {
            status = ((HTTPException) cause).getResponseCode();
         } else {
            throw e;
         }
      }
      String result;
      switch (status) {
         case 200:
            result = "authorized";
            break;
         case 401:
            result = "unauthorized";
            break;
         default:
            result = "wrong http status " + status;
      }
      System.out.println(result + " " + bp.getRequestContext().get(BindingProvider.USERNAME_PROPERTY) + " " + bp.getRequestContext().get(BindingProvider.PASSWORD_PROPERTY));
      //wait a bit before returning as the log processing can be aysnch, the test client
      //relies on the log contents and the log streams are closed by the system when the
      //process terminates
      Thread.sleep(1000);
   }
}
