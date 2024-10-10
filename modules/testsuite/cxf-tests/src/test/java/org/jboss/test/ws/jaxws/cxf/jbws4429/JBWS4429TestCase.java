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
package org.jboss.test.ws.jaxws.cxf.jbws4429;

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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

@RunWith(Arquillian.class)
public class JBWS4429TestCase extends JBossWSTest {
    private static final String DEP = "jaxws-cxf-jbws4429";

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = DEP, testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
        archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.apache.cxf org.jboss.logging \n"))
                .addClasses(HelloServiceImpl.class, LoggingHandler.class)
                .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws4429/handlers.xml")), "WEB-INF/classes/handlers.xml")
        ;
        return archive;
    }

    @Test
    @RunAsClient
    public void testWS() throws Exception {
        URL wsdlURL = JBWS4429TestCase.getResourceURL("/jaxws/cxf/jbws4429/WEB-INF/wsdl/HelloService.wsdl");
        HelloServiceService clientService = new HelloServiceService(wsdlURL);
        HelloService service = clientService.getHelloServicePort();
        try {
            service.sayHello("Jim");
            Assert.fail("sayHello() call should fail");
        } catch (Exception e) {
            Assert.assertEquals("JBWS024118: BindingOperation is missing for authorization", e.getMessage());
        }

    }


}
