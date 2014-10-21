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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test secure EJB3 endpoints using @SecurityDomain and @PermitAll, @RolesAllowed annotations.
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
 * 
 */
public class PermitAllTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS_1 = "http://" + getServerHost() + ":8080/jaxws-securityDomain-permitall/one";
   public final String TARGET_ENDPOINT_ADDRESS_2 = "http://" + getServerHost() + ":8080/jaxws-securityDomain-permitall/two";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-samples-securityDomain-permitall.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.PermitAllSecureEndpoint1Impl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.securityDomain.PermitAllSecureEndpoint2Impl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      JBossWSTestSetup testSetup = new JBossWSTestSetup(PermitAllTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/samples/securityDomain/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/samples/securityDomain/jbossws-roles.properties").getAbsolutePath());
      testSetup.addSecurityDomainRequirement("JBossWSSecurityDomainPermitAllTest", authenticationOptions);
      return testSetup;
   }

   public void testPortOne() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS_1 + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "PermitAllSecureEndpoint1Service");
      QName portName = new QName("http://org.jboss.ws/securityDomain", "PermitAllSecureEndpoint1Port");
      PermitAllSecureEndpoint port = Service.create(wsdlURL, serviceName).getPort(portName, PermitAllSecureEndpoint.class);
      
      try {
         port.echo("Hello");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }

      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "foo");
      assertEquals("Hello", port.echo("Hello"));
      
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "john");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bar");
      assertEquals("Hello", port.echo("Hello"));
   }
   
   public void testPortTwo() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS_2 + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "PermitAllSecureEndpoint2Service");
      QName portName = new QName("http://org.jboss.ws/securityDomain", "PermitAllSecureEndpoint2Port");
      PermitAllSecureEndpoint port = Service.create(wsdlURL, serviceName).getPort(portName, PermitAllSecureEndpoint.class);
      
      try {
         port.echo("Hello");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }
      try {
         port.echoForAll("Hi");
         fail("Authentication exception expected!");
      } catch (Exception e) {
         //expected web layer exception
         assertTrue(e.getMessage().contains("Could not send Message"));
         assertTrue(e.getCause().getMessage().contains("401: Unauthorized"));
      }

      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "foo");
      try {
         port.echo("Hello");
         fail("Authorization exception expected!");
      } catch (Exception e) {
         //expected EJB3 layer authorization exception
         assertTrue(e.getMessage().contains("not allowed"));
      }
      assertEquals("Hi", port.echoForAll("Hi"));
      
      ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "john");
      ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bar");
      assertEquals("Hello", port.echo("Hello"));
      assertEquals("Hi", port.echoForAll("Hi"));
   }
}
