/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.jaxrs.boot.servlet;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.jaxrs.boot.servlet.sub.AnotherApplication;
import org.jboss.test.jaxrs.boot.servlet.sub.AnotherEchoResource;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ServletDeploymentTestCase {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
	public static WebArchive createDeployments() {
		WebArchive archive = ShrinkWrap.create(WebArchive.class,
				"jaxrs-servlet.war");
	    archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-server services\n"))
				.addClasses(EchoResource.class, TestApplication.class,
						AnotherApplication.class, AnotherEchoResource.class, SetPropertyProvider.class)
				.setWebXML(JBossWSTestHelper.getWebXml(
										"<servlet>\n"
										+"  <servlet-name>JAXRS</servlet-name>\n"
									    +"  <servlet-class>org.jboss.wsf.stack.cxf.JAXRSServletExt</servlet-class>\n"
									    +"  <init-param>\n"
										+"     <param-name>javax.ws.rs.Application</param-name>\n"
							            +"     <param-value>org.jboss.test.jaxrs.boot.servlet.TestApplication</param-value>\n"
							            +"  </init-param>\n"
									    +"</servlet>\n"
										+"<servlet-mapping>\n"
										+"  <servlet-name>JAXRS</servlet-name>\n"
										+"   <url-pattern>/*</url-pattern>\n"
										+"</servlet-mapping>\n"));
		return archive;
	}
    
    @Test
    public void testRequest() throws Exception {
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url + "myjaxrs/echo/5");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("echo:5", response.readEntity(String.class));
    }
    
    
    @Test
    public void testDoubleSlashRequest() throws Exception {
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url + "/myjaxrs/echo/5");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("echo:5", response.readEntity(String.class));
    }
    
    
    
    @Test
    public void testRequestWithProvider() throws Exception {
    	
    	Client client = ClientBuilder.newClient();
    	client.register(new SetPropertyProvider(new AtomicInteger(0)));
		WebTarget target = client.target(url + "/myjaxrs/echo/5");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("echo:5", response.readEntity(String.class));
        response = builder.buildGet().invoke();
        Assert.assertEquals("Value is set from client provider", response.readEntity(String.class));
    }
    
}

