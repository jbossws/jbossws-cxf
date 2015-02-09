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
package org.jboss.test.ws.jaxws.cxf.httpproxy;

import io.netty.handler.codec.http.HttpRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpProxyServer;

import com.barchart.udt.SocketUDT;

/**
 * Tests / samples for WS client using HTTP Proxy
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-May-2011
 */
@RunWith(Arquillian.class)
public class HTTPProxyTestCaseForked extends JBossWSTest
{
   private static int proxyPort = 19387;
   private static final String PROXY_USER = "foo";
   private static final String PROXY_PWD = "bar";
   private static final String ENDPOINT_PATH = "/jaxws-cxf-httpproxy/HelloWorldService/HelloWorldImpl";
   private HttpProxyServer proxyServer;
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-httpproxy.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.httpproxy.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.httpproxy.HelloWorldImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpproxy/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testHttpProxy() throws Exception
   {
      if (checkNativeLibraries()) {
         initProxyServer();
      } else {
         return;
      }
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
   
   @Test
   @RunAsClient
   public void testHttpProxyUsingHTTPClientPolicy() throws Exception
   {
      if (checkNativeLibraries()) {
         initProxyServer();
      } else {
         return;
      }
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
   
   private void initProxyServer() throws Exception
   {
      org.littleshoot.proxy.ProxyAuthenticator proxyAuthenticator = new org.littleshoot.proxy.ProxyAuthenticator()
      {
         @Override
         public boolean authenticate(String user, String pwd)
         {
            return (PROXY_USER.equals(user) && PROXY_PWD.equals(pwd));
         }
      };
      InetSocketAddress address = new InetSocketAddress(getServerHost(), ++proxyPort);
      ChainedProxyManager chainProxyManager = new ChainedProxyManager()
      {
         @Override
         public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies)
         {
            chainedProxies.add(new ChainedProxyAdapter()
            {
               @Override
               public InetSocketAddress getChainedProxyAddress()
               {
                  return new InetSocketAddress(getServerHost(), getServerPort());
               }

            });
         }
      };
      proxyServer = org.littleshoot.proxy.impl.DefaultHttpProxyServer
                            .bootstrap()
                            .withChainProxyManager(chainProxyManager)
                            .withAddress(address)
                            .withProxyAuthenticator(proxyAuthenticator)
                            .start();
   }

   @Override
   protected void tearDown() throws Exception
   {
      clearProxySystemProperties();
      if (proxyServer != null)
      {
         proxyServer.stop();
      }
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

   public void testWSDLHttpProxy() throws Exception
   {
      if (checkNativeLibraries()) {
         initProxyServer();
      } else {
         return;
      }
      setProxySystemProperties();
      try
      {
         Authenticator.setDefault(new ProxyAuthenticator(PROXY_USER, PROXY_PWD));
         String endpointAddress = "http://unreachable-testWSDLHttpProxy" + ENDPOINT_PATH;
         String c = readContent(new URL(endpointAddress + "?wsdl")).toString();
         assertTrue(c.contains("wsdl:definitions") && c.contains(" name=\"HelloWorldService\""));
      }
      finally
      {
         Authenticator.setDefault(null);
      }
   }

   public void testWSDLNoHttpProxy() throws Exception
   {
      if (checkNativeLibraries()) {
         initProxyServer();
      } else {
         return;
      }
      clearProxySystemProperties();
      String endpointAddress = "http://unreachable-testWSDLNoHttpProxy" + ENDPOINT_PATH;
      try
      {
         readContent(new URL(endpointAddress + "?wsdl"));
         fail("Request expected to fail without http proxy");
      }
      catch (Exception e)
      {
         assertTrue(e.getMessage().contains("unreachable-testWSDLNoHttpProxy"));
      }
   }
   
   private boolean checkNativeLibraries() {
      boolean result;
      try {
         result = SocketUDT.INIT_OK;
      } catch (Throwable e) {
         result = false;
      }
      if (!result) {
         System.out.println("Native libraries not available or not loadable, skipping test. " +
         		"Check logs for more details and see https://github.com/adamfisk/LittleProxy/issues/110");
      }
      return result;
   }
   
   private static StringBuffer readContent(URL url) throws Exception
   {
      StringBuffer sb = new StringBuffer();
      InputStream is = null;
      try
      {
         is = url.openConnection().getInputStream();
         BufferedReader in = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = in.readLine()) != null)
         {
            sb.append(line);
            sb.append("\n");
         }
      }
      finally
      {
         if (is != null) is.close();
      }
      return sb;
   }
   
   private static class ProxyAuthenticator extends Authenticator
   {
      private String user, password;

      public ProxyAuthenticator(String user, String password)
      {
         this.user = user;
         this.password = password;
      }

      protected PasswordAuthentication getPasswordAuthentication()
      {
         return new PasswordAuthentication(user, password.toCharArray());
      }
   }
}
