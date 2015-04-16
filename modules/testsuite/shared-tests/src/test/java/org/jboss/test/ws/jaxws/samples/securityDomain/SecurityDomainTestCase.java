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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

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
public class SecurityDomainTestCase extends JBossWSTest
{
   private final static String DEPLOYMENT1 = "jaxws-samples-securityDomain";
   private final static String DEPLOYMENT2 = "jaxws-samples-securityDomain2";
   private final static String DEPLOYMENT3 = "jaxws-samples-securityDomain3";
   @ArquillianResource
   private URL baseURL;
   @ArquillianResource
   Deployer deployer;

   @Deployment(name= DEPLOYMENT1, testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-securityDomain.jar");
               archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.apache.cxf.impl\n")) 
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.SecureEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.EnableRobustOneWayInterceptor.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/securityDomain/jboss-webservices.xml"), "jboss-webservices.xml");
      return archive;
   }
   @Deployment(name= DEPLOYMENT2, testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-securityDomain2.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.SecureEndpointImpl2.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/securityDomain/jboss-ejb3.xml"), "jboss-ejb3.xml");
      return archive;
   }
   
   @Deployment(name= DEPLOYMENT3, testable = false)
   public static JavaArchive createDeployment3() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-securityDomain3.jar");
         archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                + "Dependencies: org.apache.cxf.impl\n")) 
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.SecureEndpointImpl3.class)
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.EnableRobustOneWayInterceptor.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/securityDomain/jboss-webservices.xml"), "jboss-webservices.xml");
      return archive;
   }

   private SecureEndpoint getAuthzPort() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "SecureEndpointService");
      return Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
   }

   
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

   @Test
   @RunAsClient
   @OperateOnDeployment(DEPLOYMENT1)
   public void testMethodLevelRolesAllowedOneWay() throws Exception
   {
      //test unthenticated
      SecureEndpoint port2 = getAuthzPort();
      testOneWay(port2);
   }   

   @Test
   @RunAsClient
   @OperateOnDeployment(DEPLOYMENT2)
   //To test missing-method-permissions-deny-access is setting to false and defaultAccess() should be allowed
   public void testEjbSecurityAuth() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-securityDomain2/authz?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "SecureEndpointService2");
      SecureEndpoint port = Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
      try {
          port.echoForAll("Hello");
          fail("Authorization exception expected!");
       } catch (Exception e) {
           //expected web layer exception
           assertTrue(e.getMessage().contains("Could not send Message"));
           assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("401: Unauthorized"));
      }
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "foo");
      port.echoForAll("Hello");
      try {
         port.restrictedEcho("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
          //expected EJB3 layer authorization exception
          assertTrue("Exception message: " + e.getMessage(), e.getMessage().contains("not allowed"));
      }
      
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kate");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "theprincess");
      assertEquals("Greetings", port.echoForAll("Greetings"));
      assertEquals("Greetings", port.echo("Greetings"));
      assertEquals("Greetings", port.restrictedEcho("Greetings"));
      assertEquals("Greetings", port.defaultAccess("Greetings"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEPLOYMENT3)
   //To test one-way operaton with class level @RolesAllowed
   public void testClassLevelRolesAllowedOneWay() throws Exception
   {
      //test unthenticated
      URL wsdlURL = new URL(baseURL + "/jaxws-securityDomain3/authz?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "SecureEndpointService3");
      SecureEndpoint port = Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
      testOneWay(port);
   }
   private void testOneWay(SecureEndpoint port) throws Exception {
      try
      {
         port.helloOneWay("Hello");
         fail("Authentication exception expected!");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue("Exception Cause message: " + e.getCause().getMessage(), e.getCause().getMessage().contains("401: Unauthorized"));
      }
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "foo");
      try {
         port.helloOneWay("Hello");
         fail("Authorization exception expected!");
      } catch (WebServiceException e) {
         //Do nothing
      }
      
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "john");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bar");
      try {
         port.helloOneWay("Hello");
         
      } catch (Exception e) {
         fail("exception is unexpected!");
      }
   }
}
