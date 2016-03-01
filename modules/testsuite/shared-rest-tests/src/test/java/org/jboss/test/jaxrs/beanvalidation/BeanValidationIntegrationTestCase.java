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
package org.jboss.test.jaxrs.beanvalidation;

import java.net.URL;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the integration of JAX-RS and Bean Validation.
 *
 * @author Stuart Douglas
 * @author Gunnar Morling
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class BeanValidationIntegrationTestCase {

	private Client client = ClientBuilder.newClient();
	
    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployments()
    {
       WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-beanvalidation.war");
       archive.addManifest()
          .addClasses(TestApplication.class, ValidatorModel.class, ValidatorResource.class,
                AnotherValidatorResource.class, YetAnotherValidatorResource.class);
       return archive;
    }
    
    @Test
    public void testValidRequest() throws Exception {
		WebTarget target = client.target(url + "myjaxrs/validate/5");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
    	
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.readEntity(String.class).contains("<validator id=\"5\"/>"));
    }

    @Test
    public void testInvalidRequest() throws Exception {
		WebTarget target = client.target(url + "myjaxrs/validate/3");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("Parameter constraint violated", 400, response.getStatus());
    }

    @Test
    public void testInvalidRequestCausingPropertyConstraintViolation() throws Exception {
		WebTarget target = client.target(url + "myjaxrs/another-validate/3");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("Property constraint violated", 400, response.getStatus());
    }

    @Test
    public void testInvalidRequestsAreAcceptedDependingOnValidationConfiguration() throws Exception {
		WebTarget target = client.target(url + "myjaxrs/yet-another-validate/3/disabled");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("No constraint violated", 200, response.getStatus());

        target = client.target(url + "myjaxrs/yet-another-validate/3/enabled");
		response = target.request().buildGet().invoke();
        Assert.assertEquals("Parameter constraint violated", 400, response.getStatus());
    }

    @Test
    public void testInvalidResponse() throws Exception {
		WebTarget target = client.target(url + "myjaxrs/validate/11");
		Invocation.Builder builder = target.request();
		Response response = builder.buildGet().invoke();
        Assert.assertEquals("Return value constraint violated", 500,  response.getStatus());
    }
}
