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
package org.jboss.test.ws.jaxws.cxf.httpproxy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthorizationHandler;

/**
 * Tests / samples for WS client using HTTP Proxy
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-May-2011
 */
public class HTTPProxyTestCase extends JBossWSTest
{
   private static int proxyPort = 19387;
   private static final String PROXY_USER = "foo";
   private static final String PROXY_PWD = "bar";
   private HttpProxyServer proxyServer;

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(HTTPProxyTestCase.class, "jaxws-cxf-httpproxy.war");
   }

   public void testHttpProxy() throws Exception
   {
      final String testHost = "unreachable-testHttpProxy";
      HelloWorld port = getPort(getResourceURL("jaxws/cxf/httpproxy/HelloWorldService.wsdl"), testHost);
      final String hi = "Hi!";
      //first try without setting up the proxy -> request fails because the host is not known/reachable
      try
      {
         port.echo(hi);
         fail("Exception expected");
      }
      catch (Exception e)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         e.printStackTrace(new PrintStream(baos));
         assertTrue(baos.toString().contains(testHost));
      }
      
      //then setup the proxy, but provide no authentication/authorization info -> request fails because of HTTP 407
      setProxySystemProperties();
      try
      {
         port.echo(hi);
         fail("Exception expected");
      }
      catch (Exception e)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         e.printStackTrace(new PrintStream(baos));
         assertTrue(baos.toString().contains("407: Proxy Authentication Required"));
      }
      
      //finally setup everything
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      ProxyAuthorizationPolicy policy = new ProxyAuthorizationPolicy();
      policy.setAuthorizationType("Basic");
      policy.setUserName(PROXY_USER);
      policy.setPassword(PROXY_PWD);
      conduit.setProxyAuthorization(policy);
      
      assertEquals(hi, port.echo(hi));
   }
   
   public void testHttpProxyUsingHTTPClientPolicy() throws Exception
   {
      final String testHost = "unreachable-testHttpProxyUsingHTTPClientPolicy";
      HelloWorld port = getPort(getResourceURL("jaxws/cxf/httpproxy/HelloWorldService.wsdl"), testHost);
      final String hi = "Hi!";
      //first try without setting up the proxy -> request fails because the host is not known/reachable
      try
      {
         port.echo(hi);
         fail("Exception expected");
      }
      catch (Exception e)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         e.printStackTrace(new PrintStream(baos));
         assertTrue(baos.toString().contains(testHost));
      }
      
      //then setup the proxy, but provide no authentication/authorization info -> request fails because of HTTP 407
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy clientPolicy = conduit.getClient();
      clientPolicy.setProxyServerType(ProxyServerType.HTTP);
      clientPolicy.setProxyServer(getServerHost());
      clientPolicy.setProxyServerPort(proxyPort);
      try
      {
         port.echo(hi);
         fail("Exception expected");
      }
      catch (Exception e)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         e.printStackTrace(new PrintStream(baos));
         assertTrue(baos.toString().contains("407: Proxy Authentication Required"));
      }
      
      //finally setup authorization info too
      ProxyAuthorizationPolicy authPolicy = new ProxyAuthorizationPolicy();
      authPolicy.setAuthorizationType("Basic");
      authPolicy.setUserName(PROXY_USER);
      authPolicy.setPassword(PROXY_PWD);
      conduit.setProxyAuthorization(authPolicy);
      
      assertEquals(hi, port.echo(hi));
   }
   
   @Override
   protected void setUp() throws Exception
   {
      proxyServer = new DefaultHttpProxyServer(++proxyPort, new HashMap<String, HttpFilter>(),
            getServerHost() + ":8080", null, null);
      ProxyAuthorizationHandler authorizationHandler = new ProxyAuthorizationHandler()
      {

         @Override
         public boolean authenticate(String user, String pwd)
         {
            return (PROXY_USER.equals(user) && PROXY_PWD.equals(pwd));
         }
      };
      proxyServer.addProxyAuthenticationHandler(authorizationHandler);
      proxyServer.start();
   }

   @Override
   protected void tearDown() throws Exception
   {
      if (proxyServer != null)
      {
         proxyServer.stop();
      }
      clearProxySystemProperties();
   }
   
   private HelloWorld getPort(URL wsdlURL, String endpointAddressHost) throws MalformedURLException
   {
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/httpproxy", "HelloWorldService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/httpproxy", "HelloWorldImplPort");
      HelloWorld port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
      BindingProvider provider = (BindingProvider)port;
      provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://" + endpointAddressHost + "/jaxws-cxf-httpproxy/HelloWorldService/HelloWorldImpl");
      return port;
   }
   
   private static void setProxySystemProperties()
   {
      System.getProperties().setProperty("http.proxyHost", getServerHost());
      System.getProperties().setProperty("http.proxyPort", String.valueOf(proxyPort));
   }
   
   private static void clearProxySystemProperties()
   {
      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
   }

}
