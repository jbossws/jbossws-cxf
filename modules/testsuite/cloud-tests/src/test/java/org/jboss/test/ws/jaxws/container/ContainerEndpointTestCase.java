/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2023, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

    public static final GenericContainer<?> jbosswsContainer = new GenericContainer<>(DockerImageName.parse("jbossws-cxf-cloud-tests:latest")).withExposedPorts(8080);

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