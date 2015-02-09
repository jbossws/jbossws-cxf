/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.securityDomain;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test secure EJB3 endpoints using @SecurityDomain and @RolesAllowed, @DeclaredRoles annotations.
 * 
 * The security domain the application is associated with comes with a UsersRolesLoginModule and has the following users:
 * 
 * username  password    roles
 * --------- ----------- -----------------
 * bob       foo         user
 * john      bar         user,friend
 * kate      theprincess user,friend,royal
 * 
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class SecurityDomainTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name="jaxws-samples-securityDomain", testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-securityDomain.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.SecureEndpointImpl.class);
      return archive;
   }

   private SecureEndpoint getAuthzPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-securityDomain/authz?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "SecureEndpointService");
      return Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
   }

   @Test
   @RunAsClient
   public void testUnauthenticated() throws Exception
   {
      SecureEndpoint port1 = getAuthzPort();
      
      try {
         port1.echoForAll("Hello");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("401: Unauthorized"));
      }
      
      try {
         port1.echo("Hello");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("401: Unauthorized"));
      }
      
      try {
         port1.restrictedEcho("Hello");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("401: Unauthorized"));
      }
   }

   @Test
   @RunAsClient
   public void testUnauthorized() throws Exception
   {
      SecureEndpoint port2 = getAuthzPort();
      ((BindingProvider)port2).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "john");
      ((BindingProvider)port2).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bar");
      try {
         port2.restrictedEcho("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         //expected EJB3 layer authorization exception
         assertTrue("Exception message: " + e.getMessage(), e.getMessage().contains("not allowed"));
      }
   }

   @Test
   @RunAsClient
   public void testAuthorizedAccess() throws Exception
   {
      SecureEndpoint port = getAuthzPort();

      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "john");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bar");
      assertEquals("Hello", port.echoForAll("Hello"));
      assertEquals("Hello", port.echo("Hello"));
      
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kate");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "theprincess");
      assertEquals("Greetings", port.echoForAll("Greetings"));
      assertEquals("Greetings", port.echo("Greetings"));
      assertEquals("Greetings", port.restrictedEcho("Greetings"));
   }

   @Test
   @RunAsClient
   public void testUndeclaredRole() throws Exception
   {
      SecureEndpoint port = getAuthzPort();
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "foo");
      try {
         port.echoForAll("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("403: Forbidden"));
      }
      try {
         port.echo("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("403: Forbidden"));
      }
      try {
         port.restrictedEcho("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("403: Forbidden"));
      }
   }
   
}