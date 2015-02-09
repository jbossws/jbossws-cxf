/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.runner.RunWith;

/**
 * Test JAX-WS exception handling
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class ExceptionTestCase extends AbstractExceptionTests
{
   @Deployment(name="jaxws-samples-exception-jse-plain", testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-jse-plain.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12ExceptionEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.UserException.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/exception/server/jaxws-handlers-server.xml")
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultException.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultExceptionResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.UserExceptionBean.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/WEB-INF/web.xml"));
      return archive;
   }
   
   protected ExceptionHelper getHelper()
   {
      return new ExceptionHelper("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-exception-jse-plain/ExceptionEndpointService");
   }
   
   protected SOAP12ExceptionHelper getSOAP12Helper()
   {
      return new SOAP12ExceptionHelper("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-exception-jse-plain/SOAP12ExceptionEndpointService");
   }
}
