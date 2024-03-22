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
package org.jboss.test.ws.jaxws.cxf.http2;

import jakarta.xml.ws.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import org.apache.cxf.BusFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class Http2EndpointTestCaseForked extends JBossWSTest {
    @ArquillianResource
    private URL baseURL;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-http2.war")
                .addClass(HelloWorld.class)
                .addClass(HelloWorldImpl.class)
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/http2/WEB-INF/web.xml"));
        return archive;
    }

    @Test
    @RunAsClient
    public void testHttp2() throws Exception {
        BusFactory.setDefaultBus(null);
        BusFactory.getDefaultBus().setProperty("org.apache.cxf.transport.http.forceVersion", "2");
        //enable jdk httpclient debug and it prints debug log message to system.err
        System.setProperty("jdk.internal.httpclient.debug", "true");
        PrintStream old = System.err;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            System.setErr(ps);
            HelloWorld port = getPort();
            String response = port.echo("hello");
            Assertions.assertEquals("hello", response,"hello is expected");
            ps.flush();
            Assertions.assertTrue( baos.toString().contains("ExchangeImpl get: Trying to get HTTP/2 connection"), "HTTP2Connection is expected to use");
        }finally {
            System.setErr(old);
            System.clearProperty("jdk.internal.httpclient.debug");
        }
    }

    @Test
    @RunAsClient
    public void testHttp11() throws Exception {
        System.setProperty("jdk.internal.httpclient.debug", "true");
        BusFactory.setDefaultBus(null);
        BusFactory.getDefaultBus().setProperty("org.apache.cxf.transport.http.forceVersion", "1.1");
        PrintStream old = System.err;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            System.setErr(ps);
            HelloWorld port = getPort();
            String response = port.echo("hello");
            Assertions.assertEquals("hello", response,"hello is expected");
            ps.flush();
            Assertions.assertTrue(baos.toString().contains("ExchangeImpl get: HTTP/1.1: new Http1Exchange"), "HTTP2Connection isn't expected to use");
        }finally {
            System.setErr(old);
            System.clearProperty("jdk.internal.httpclient.debug");
        }
    }

    private HelloWorld getPort() throws MalformedURLException
    {
        URL wsdlURL = new URL(baseURL.toString() + "/jaxws-cxf-http2?wsdl");
        QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/http2", "HelloWorldService");
        Service service = Service.create(wsdlURL, serviceName);
        QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/http2", "HelloWorldImplPort");
        HelloWorld port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
        return port;
    }
}