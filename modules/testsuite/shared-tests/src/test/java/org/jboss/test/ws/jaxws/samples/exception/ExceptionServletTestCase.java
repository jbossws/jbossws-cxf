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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExceptionServletTestCase extends JBossWSTest
{
   @Deployment(name="jaxws-samples-exception-client", testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-client.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.helper.ClientHelper.class)
         .addClass(org.jboss.test.helper.TestServlet.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.ExceptionHelper.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.SOAP12ExceptionHelper.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/handlerchain/WEB-INF/permissions.xml"), "permissions.xml")
         .addPackage("org.jboss.test.ws.jaxws.samples.exception.client");
      return archive;
   }

   @Deployment(name = "jaxws-samples-exception-jse", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-jse.war");
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

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testRuntimeException() throws Exception
   {
      assertEquals("1", runTestInContainer("testRuntimeException"));
   }

   public void testSoapFaultException() throws Exception
   {
      assertEquals("1", runTestInContainer("testSoapFaultException"));
   }

   public void testApplicationException() throws Exception
   {
      assertEquals("1", runTestInContainer("testApplicationException"));
   }
   
   public void testRuntimeExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testRuntimeException"));
   }

   public void testSoapFaultExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testSoapFaultException"));
   }

   public void testApplicationExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testApplicationException"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":" + getServerPort() + "/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/ExceptionEndpointService&method=" + test
            + "&helper=" + ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
   
   private String runTestInContainerSOAP12(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":" + getServerPort() + "/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/SOAP12ExceptionEndpointService&method=" + test
            + "&helper=" + SOAP12ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
