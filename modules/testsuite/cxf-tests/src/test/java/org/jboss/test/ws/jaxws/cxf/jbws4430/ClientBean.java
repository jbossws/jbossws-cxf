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
package org.jboss.test.ws.jaxws.cxf.jbws4430;

import java.net.URL;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.xml.ws.Service;

@jakarta.jws.WebService(targetNamespace = "http://test.ws.jboss.org/", wsdlLocation = "WEB-INF/wsdl/ClientBeanService.wsdl", serviceName = "ClientBeanService")
public class ClientBean {

    private static final Logger logger = Logger.getLogger(ClientBean.class.getName());

    public ClientBean() {
    }

    private void testDelegateBean() {

        CDI cdi = CDI.current();
        if (cdi == null)
            throw new RuntimeException("Unable to get CDI.current");

        DelegateBean delegateBean = (DelegateBean) cdi.select(DelegateBean.class).get();
        if (delegateBean == null)
            throw new RuntimeException("Unable to get DelegateBean via CDI");

        logger.info("delegateBean = " + delegateBean);
    }

    @jakarta.jws.WebMethod
    public String hello(URL baseURL, String name) throws Exception {

        try {

            testDelegateBean();

            QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
            QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");

            URL wsdlURL = new URL(baseURL + "HelloBean?wsdl");

            logger.info("");
            Service service = Service.create(wsdlURL, serviceName);
            service.setHandlerResolver(new AccessTokenClientHandlerResolver("x", "y"));
            Hello proxy = service.getPort(portName, Hello.class);

            return proxy.hello(name);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}