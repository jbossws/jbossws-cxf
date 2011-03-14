/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.endpoint;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public class EndpointTestCase extends JBossWSTest
{
    private static String publishURL = "http://" + getServerHost() + ":18080/HelloWorldService";

    public static Test suite()
    {
        return new JBossWSTestSetup(EndpointTestCase.class, "");
    }

    public void testClassLoader() throws Exception
    {
        deploy("jaxws-cxf-endpoint.war");
        HelloWorld port = this.getProxy(publishURL);
        String classLoader1 = port.getClassLoader();
        String deploymentClassLoader1 = port.getDeploymentClassLoader();
        undeploy("jaxws-cxf-endpoint.war");
        assertEquals(classLoader1, deploymentClassLoader1);
        deploy("jaxws-cxf-endpoint.war");
        port = this.getProxy(publishURL);
        String classLoader2 = port.getClassLoader();
        String deploymentClassLoader2 = port.getDeploymentClassLoader();
        undeploy("jaxws-cxf-endpoint.war");
        assertEquals(classLoader2, deploymentClassLoader2);
        assertFalse(classLoader1.equals(classLoader2));
    }

    private HelloWorld getProxy(String publishURL) throws Exception
    {
        URL wsdlURL = new URL(publishURL + "?wsdl");
        QName qname = new QName("http://org.jboss.ws/jaxws/cxf/endpoint", "HelloWorldService");
        Service service = Service.create(wsdlURL, qname);
        return (HelloWorld)service.getPort(HelloWorld.class);
    }
}
