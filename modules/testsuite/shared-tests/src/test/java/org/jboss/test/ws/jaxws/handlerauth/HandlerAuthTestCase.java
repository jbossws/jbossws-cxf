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
package org.jboss.test.ws.jaxws.handlerauth;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test proper handler auth
 *
 * @author Alessio Soldano
 * @since 24-Sep-2013
 */
public class HandlerAuthTestCase extends JBossWSTest
{
   public static Test suite()
   {
      JBossWSTestSetup testSetup = new JBossWSTestSetup(HandlerAuthTestCase.class, "jaxws-handlerauth.jar,jaxws-handlerauth2.jar,jaxws-handlerauth3.jar");
      Map<String, String> authenticationOptions = new HashMap<String, String>();
      authenticationOptions.put("usersProperties",
            getResourceFile("jaxws/handlerauth/jbossws-users.properties").getAbsolutePath());
      authenticationOptions.put("rolesProperties",
            getResourceFile("jaxws/handlerauth/jbossws-roles.properties").getAbsolutePath());
      testSetup.addSecurityDomainRequirement("handlerauth-security-domain", authenticationOptions);
      return testSetup;
   }
   
   public void testAuthSOAPHandler() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImplService"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpointPort"), SecureEndpoint.class);
      testAuth(port);
   }

   public void testAuthLogicalHandler() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth2?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImpl2Service"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpoint2Port"), SecureEndpoint.class);
      testAuth(port);
   }

   public void testNoHandlerAuth() throws Exception {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/handlerauth3?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws/", "SecureEndpointImpl3Service"));
      SecureEndpoint port = service.getPort(new QName("http://ws/", "SecureEndpoint3Port"), SecureEndpoint.class);
      setUser((BindingProvider)port, "John", "foo");
      int count = port.getHandlerCounter();
      int newCount;
      
      assertEquals("Hello, Mr. John", port.sayHello("John"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      assertEquals("Bye, Mr. John", port.sayBye("John"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         assertEquals(++count, newCount); //verify count is increased
      }
      
      port.ping();
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      assertEquals("foo", port.echo("foo"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      
      //Change user...
      setUser((BindingProvider)port, "Bob", "bar");
      
      assertEquals("Hello, Mr. Bob", port.sayHello("Bob"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      try {
         port.sayBye("Bob");
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         assertEquals(++count, newCount); //verify count is increased
      }
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         newCount = port.getHandlerCounter();
         assertEquals(++count, newCount); //verify count is increased
      }
      
      assertEquals("foo2", port.echo("foo2"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
   }

   private void testAuth(final SecureEndpoint port) throws Exception
   {
      setUser((BindingProvider)port, "John", "foo");
      int count = port.getHandlerCounter();
      int newCount;
      
      assertEquals("Hello, Mr. John", port.sayHello("John"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      assertEquals("Bye, Mr. John", port.sayBye("John"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         assertEquals(count, newCount); //verify count is *not* increased
      }
      
      port.ping();
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      assertEquals("foo", port.echo("foo"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      
      //Change user...
      setUser((BindingProvider)port, "Bob", "bar");
      
      assertEquals("Hello, Mr. Bob", port.sayHello("Bob"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
      
      try {
         port.sayBye("Bob");
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         assertEquals(count, newCount); //verify count is *not* increased
      }
      
      try {
         port.deniedMethod();
         fail("Exception expected!");
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         assertEquals(count, newCount); //verify count is *not* increased
      }
      
      try {
         port.ping();
      } catch (Exception e) {
         assertTrue(e.getMessage().contains("JBWS024094"));
         newCount = port.getHandlerCounter();
         assertEquals(count, newCount); //verify count is *not* increased
      }
      
      assertEquals("foo2", port.echo("foo2"));
      newCount = port.getHandlerCounter();
      assertEquals(++count, newCount);
   }
   
   private static void setUser(BindingProvider bp, String username, String password) {
      bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
      bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
   }
}
