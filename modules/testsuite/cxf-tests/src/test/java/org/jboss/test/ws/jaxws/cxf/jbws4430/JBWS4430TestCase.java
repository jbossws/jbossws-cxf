/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2024, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws4430;

import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS4430TestCase extends JBossWSTest {
    private static final String DEP = "jaxws-cxf-jbws4430";

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = DEP, testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
        archive.setManifest(new StringAsset(
                "Manifest-Version: 1.0\n" + "Dependencies: org.apache.cxf,org.jboss.ws.cxf.jbossws-cxf-server\n"))
                .addClasses(ClientBean.class, Hello.class, HelloBean.class, DelegateBean.class, EmptyBean.class, CDIOutInterceptor.class,
                        LoggingHandler.class, AccessTokenClientHandler.class, AccessTokenClientHandlerResolver.class,
                        CredentialsCDIBean.class)
                .addAsWebInfResource(
                        new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4430/WEB-INF/beans.xml"),
                        "beans.xml")
                .addAsWebInfResource(
                        new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4430/WEB-INF/wsdl/HelloWorld.wsdl"),
                        "wsdl/HelloWorld.wsdl")
                .addAsWebInfResource(new File(
                        JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4430/WEB-INF/wsdl/ClientBeanService.wsdl"),
                        "wsdl/ClientBeanService.wsdl")
                .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4430/handlers.xml")),
                        "WEB-INF/classes/handlers.xml");
        return archive;
    }

    @Test
    @RunAsClient
    public void testWS() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "HelloBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "HelloBeanPort");

        URL wsdlURL = new URL(baseURL + "HelloBean?wsdl");

        Service service = Service.create(wsdlURL, serviceName);
        Hello proxy = service.getPort(portName, Hello.class);
        assertEquals("Hello jbossws", proxy.hello("jbossws"));
        try {
            proxy.hello("");
            fail("An exception is expected to test the LoggingHandler.handleFault()");
        } catch (Exception e) {
            if (!(e instanceof SOAPFaultException)) {
                fail("unexpected exception " + e);
            }
        }

    }

    @Test
    @RunAsClient
    public void testClientWS() throws Exception {
        QName serviceName = new QName("http://test.ws.jboss.org/", "ClientBeanService");
        QName portName = new QName("http://test.ws.jboss.org/", "ClientBeanPort");

        URL wsdlURL = new URL(baseURL + "ClientBeanService?wsdl");

        Service service = Service.create(wsdlURL, serviceName);
        Client proxy = service.getPort(portName, Client.class);
        assertEquals("Hello jbossws", proxy.hello(baseURL, "jbossws"));
    }
}