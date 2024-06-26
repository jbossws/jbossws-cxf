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
package org.jboss.test.ws.jaxws.cxf.throttling;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class ThrottlingTestCase extends JBossWSTest {
    private static final String  THROTTLING = "jaxws-cxf-throttling";

    private static final String  PERIOD = "jaxws-cxf-throttling-peroid";
    @ArquillianResource
    private URL baseURL;

    @Deployment(name=THROTTLING, testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-throttling.war")
                .addClass(HelloWorld.class)
                .addClass(HelloWorldImpl.class)
                .addClass(Hello.class)
                .addClass(HelloImpl.class)
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/throttling/WEB-INF/web.xml"))
                .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/throttling/WEB-INF/jaxws-endpoint-config.xml")), "jaxws-endpoint-config.xml");
        return archive;
    }

    @Deployment(name=PERIOD, testable = false)
    public static WebArchive createPeriodDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-throttling-period.war")
                .addClass(Hello.class)
                .addClass(HelloImpl.class)
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/throttling/WEB-INF/web2.xml"))
                .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/throttling/WEB-INF/jaxws-endpoint-config2.xml")), "jaxws-endpoint-config.xml");
        return archive;
    }


    @Test
    @RunAsClient
    @OperateOnDeployment(THROTTLING)
    public void testThrottlingWithDefaultManager() throws Exception {
        //Throttling feature only allows 5 invocations and getPort access wsdl already
        //called for once.
        HelloWorld port = getHelloWorldPort();
        for (int i = 0; i < 4; i++) {
            String response = port.echo("hello");
            Assertions.assertEquals("hello", response, "hello is expected");
        }

        try {
            String response = port.echo("hello");
            fail("Exception not thrown");
        } catch (jakarta.xml.ws.WebServiceException e) {
            Assertions.assertEquals(((BindingProvider)port).getResponseContext().get("jakarta.xml.ws.http.response.code"), 429);
        }
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(THROTTLING)
    public void testThrottlingWithTestManager() throws Exception {
        Hello port = getHelloPort();
        for (int i=0; i < 4; i++) {
            port.sayHello("hello");
        }
        try {
            String res = port.sayHello("hello");
            fail("Exception not thrown");
        } catch (jakarta.xml.ws.WebServiceException e) {
            Assertions.assertEquals(((BindingProvider)port).getResponseContext().get("jakarta.xml.ws.http.response.code"), 429);
        }
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(PERIOD)
    public void testThrottlingWithPeriodConfig() throws Exception {
        Hello port = getHelloPort();
        for (int i=0; i < 4; i++) {
            port.sayHello("hello");
        }
        try {
            String res = port.sayHello("hello");
            fail("Exception not thrown");
        } catch (jakarta.xml.ws.WebServiceException e) {
            Assertions.assertEquals(((BindingProvider)port).getResponseContext().get("jakarta.xml.ws.http.response.code"), 429);
        }
    }

    private HelloWorld getHelloWorldPort() throws MalformedURLException
    {
        URL wsdlURL = new URL(baseURL.toString() + "/helloworld?wsdl");
        QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/throttling", "HelloWorldService");
        Service service = Service.create(wsdlURL, serviceName);
        QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/throttling", "HelloWorldImplPort");
        HelloWorld port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
        return port;
    }

    private Hello getHelloPort() throws MalformedURLException
    {
        URL wsdlURL = new URL(baseURL.toString() + "/hello?wsdl");
        QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/throttling/hello", "HelloService");
        Service service = Service.create(wsdlURL, serviceName);
        QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/throttling/hello", "HelloImplPort");
        Hello port = (Hello) service.getPort(portQName, Hello.class);
        return port;
    }

}