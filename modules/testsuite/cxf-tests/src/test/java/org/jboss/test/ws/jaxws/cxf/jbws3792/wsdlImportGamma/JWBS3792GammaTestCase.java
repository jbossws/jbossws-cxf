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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportGamma;

import junit.framework.Test;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *  Test imported wsdl and its referenced schema is NOT co-located with the
 *  importing wsdl.  The imported wsdl and its referenced schema are located
 *  in the same directory as the importing wsdl.
 */
public class JWBS3792GammaTestCase extends JBossWSTest {

    public static JBossWSTestHelper.BaseDeployment<?>[] createDeployments () {
        List<JBossWSTestHelper.BaseDeployment<?>> list = new LinkedList<JBossWSTestHelper.BaseDeployment<?>>();

        list.add(
            new JBossWSTestHelper.WarDeployment("jbws3792-wsdlImportGamma.war") {
            { archive
                .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                    + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportGamma.GreetingsWsImpl.class)
                .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportGamma.GreetingsWs.class)
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                    + "/jaxws/cxf/jbws3792/wsdlImportGamma/WEB-INF/wsdl/Greeting_Simplest.wsdl"),
                    "wsdl/Greeting_Simplest.wsdl")
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                    + "/jaxws/cxf/jbws3792/wsdlImportGamma/WEB-INF/wsdl/A/b/Hello.wsdl"), "wsdl/A/b/Hello.wsdl")
                .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir()
                    + "/jaxws/cxf/jbws3792/wsdlImportGamma/WEB-INF/wsdl/A/b/Hello_schema2.xsd"), "wsdl/A/b/Hello_schema2.xsd")
            ;

            }
        });

        return list.toArray(new JBossWSTestHelper.BaseDeployment<?>[list.size()]);
    }

    public static Test suite()
    {
        return new JBossWSTestSetup(JWBS3792GammaTestCase.class,
            JBossWSTestHelper.writeToFile(createDeployments()));
    }


    public void testImportSimplest() throws Exception {
        URL wsdlURL = new URL("http://" + getServerHost()
            + ":8080/jbws3792-wsdlImportGamma/GreetingsService?wsdl");
        QName qname = new QName("http://hello/test", "HelloService");
        Service service = Service.create(wsdlURL, qname);
        Iterator<QName> it = service.getPorts();
        int cnt = 0;
        while (it.hasNext()){
            cnt++;
            QName qn = (QName)it.next();
            assertTrue("{http://hello/test}HelloServicePort".equals(qn.toString()));
        }
        assertTrue("Expected cnt to be 1 but cnt is " + cnt, cnt == 1);
    }
}
