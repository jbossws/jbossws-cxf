/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon;

import junit.framework.Test;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloRequest;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloResponse;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWs;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Test imported wsdl identified by URL of deployed app.
 */
public class JWBS3792EpsilonTestCase extends JBossWSTest {

    public static JBossWSTestHelper.BaseDeployment<?>[] createDeployments () {
        List<JBossWSTestHelper.BaseDeployment<?>> list = new LinkedList<JBossWSTestHelper.BaseDeployment<?>>();

        list.add(
            new JBossWSTestHelper.WarDeployment("jbws3792-hello.war") {
                {
                    archive
                        .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                            + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
                        .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloResponse.class)
                        .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloRequest.class)
                        .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWSImpl.class)
                        .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWs.class)
                        .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                            + "/jaxws/cxf/jbws3792/WEB-INF/wsdl/Hello.wsdl"),
                            "wsdl/Hello.wsdl")
                        .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                            + "/jaxws/cxf/jbws3792/WEB-INF/wsdl/Hello_schema2.xsd"),
                            "wsdl/Hello_schema2.xsd")
                    ;
                }
            });

        list.add(
            new JBossWSTestHelper.WarDeployment("jbws3792-wsdlImportEpsilon.war") {
            { archive
                .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                    + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon.GreetingsWsImpl.class)
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon.GreetingsWs.class)
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                    + "/jaxws/cxf/jbws3792/wsdlImportEpsilon/WEB-INF/wsdl/Greeting_Simplest.wsdl"),
                    "wsdl/Greeting_Simplest.wsdl")
            ;
            }
        });

        return list.toArray(new JBossWSTestHelper.BaseDeployment<?>[list.size()]);
    }

    public static Test suite()
    {
        return new JBossWSTestSetup(JWBS3792EpsilonTestCase.class,
            JBossWSTestHelper.writeToFile(createDeployments()));
    }


    public void testImportSimplest() throws Exception {
        URL wsdlURL = new URL("http://" + getServerHost()
            + ":8080/jbws3792-wsdlImportEpsilon/GreetingsService?wsdl");
        QName qname = new QName("http://hello/test", "HelloService");
        Service service = Service.create(wsdlURL, qname);
        HelloWs hello = (HelloWs)service.getPort(HelloWs.class);
        HelloRequest hReq = new HelloRequest();
        hReq.setInput("Joe");
        HelloResponse hRep = hello.doHello(hReq);
        assertEquals("Joe", hRep.getMultiHello().get(0));
    }
}
