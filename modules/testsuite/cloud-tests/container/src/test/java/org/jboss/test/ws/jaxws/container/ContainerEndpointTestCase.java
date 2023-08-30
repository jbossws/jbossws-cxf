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
package org.jboss.test.ws.jaxws.container;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import javax.xml.namespace.QName;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URL;
import org.testcontainers.utility.DockerImageName;
/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class ContainerEndpointTestCase {

    public static final GenericContainer<?> jbosswsContainer = new GenericContainer<>(DockerImageName.parse("jbossws-cxf-container-tests:latest")).withExposedPorts(8080);

    @BeforeClass
    public static void setUp() throws IOException {
       jbosswsContainer.start();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        jbosswsContainer.stop();
    }

    @Test
    public void testEcho() throws Exception {
        URL baseURL = new URL("http://" + jbosswsContainer.getHost() + ":" + jbosswsContainer.getFirstMappedPort() + "/echo-ws/EndpointImpl");
        Endpoint endpoint = initPort(baseURL);
        //we need a modify address config in WFLY
        ((BindingProvider)endpoint).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString());
        String result = endpoint.echo("from docker container");
        Assert.assertEquals("Unexpected result", result, "Echo:from docker container");
    }
    private Endpoint initPort(URL baseUrl) throws Exception {
        QName serviceName = new QName("http://org.jboss.ws/cxf/container", "EndpointImplService");
        URL wsdlURL = new URL(baseUrl + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName);
        Endpoint proxy = service.getPort(Endpoint.class);
        return proxy;
    }
}