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
package org.jboss.test.ws.jaxws.cxf.jbws4046;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import java.io.File;
import java.net.URL;

/**
 * @author bspyrkos@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS4046TestCase extends JBossWSTest {

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = "jbws4046", testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "jbws4046.war");
        archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                + "Dependencies: org.apache.cxf\n"))
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws4046.v1.Demo.class)
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws4046.v2.Demo.class)
                .addClass(DemoInterface.class)
                .addClass(DemoServiceHandler.class)
                .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4046/handlers.xml")), "WEB-INF/classes/handlers.xml")
                .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4046/WEB-INF/web.xml"));
        return archive;
    }

    @Test
    @RunAsClient
    public void testServiceHandlerInitializationWithInterface() throws Exception {
        String v1Resp = callVersionService("1");
        String v2Resp = callVersionService("2");

        Assert.assertNotNull("Service version 1 failed", v1Resp);
        Assert.assertNotNull("Service version 2 failed", v2Resp);
    }

    public String callVersionService(String path) throws Exception {
        URL wsdlURL = new URL(baseURL.toString() + "/" + path + "?wsdl");
        QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jbws4046", "DemoService");
        Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
        QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/jbws4046", "DemoServicePort");
        return service.getPort(portQName, DemoInterface.class, (WebServiceFeature)null).version();
    }
}
