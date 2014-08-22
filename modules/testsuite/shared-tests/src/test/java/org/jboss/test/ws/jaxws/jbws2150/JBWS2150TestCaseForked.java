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
package org.jboss.test.ws.jaxws.jbws2150;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import junit.framework.Test;

import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

/**
 * [JBWS-2150] Migrate AddressRewritingTestCase to jaxws
 * 
 * soap:address rewrite tests
 * 
 * Note: most of the tests here perform additional checks when the testsuite configured server host
 * is "localhost" (which is the default anyway) by getting wsdls and invoking endpoints using the
 * equivalent 127.0.0.1 IP. This is basically a shortcut to avoid requiring to add DNS entries or
 * binding the AS to multiple addresses before verifying address rewrite mechanism.
 * 
 * 
 * @author richard.opalka@jboss.com
 * @author alessio.soldano@jboss.com
 */
public final class JBWS2150TestCaseForked extends JBossWSTest
{
   private static final ObjectName SERVER_CONFIG_OBJECT_NAME = ObjectNameFactory.create("jboss.ws:service=ServerConfig");
   private static final String NAMESPACE = "http://test.jboss.org/addressrewrite";
   private static final String NAMESPACE_IMP = "http://test.jboss.org/addressrewrite/wsdlimp";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2150.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2150.InvalidSecureService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.InvalidService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ValidSecureService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ValidService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/wsdl/Service.wsdl"), "wsdl/Service.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/wsdl/inner.wsdl"), "wsdl/inner.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2150-codefirst.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2150.CodeFirstService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ServiceIface.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2150-sec.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2150.InvalidSecureService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.InvalidService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ValidSecureService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2150.ValidService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/wsdl/Service.wsdl"), "wsdl/Service.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/wsdl/inner.wsdl"), "wsdl/inner.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2150/WEB-INF/web-sec.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   static {
      JBossWSTestHelper.writeToFile(createDeployments());
   }

   protected static class JBWS2150TestSetup extends JBossWSTestSetup {
      
      protected static Boolean modifySOAPAddress;
      protected static String webServiceHost;
      protected static int webServicePort;
      protected static int webServiceSecurePort;
      protected static String webServicePath;
      protected static String webServiceUriScheme;
      
      public JBWS2150TestSetup() {
         super(JBWS2150TestCaseForked.class, null);
      }

      public void setUp() throws Exception
      {
         modifySOAPAddress = (Boolean)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "ModifySOAPAddress");
         webServiceHost = (String)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServiceHost");
         webServicePort = (Integer)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServicePort");
         webServiceSecurePort = (Integer)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServiceSecurePort");
         webServicePath = (String)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServicePathRewriteRule");
         webServiceUriScheme = (String)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServiceUriScheme");
         super.setUp();
      }
   }

   public static Test suite()
   {
      return new JBWS2150TestSetup();
   }
   
   public void tearDown() throws Exception
   {
      Attribute attr = new Attribute("ModifySOAPAddress", JBWS2150TestSetup.modifySOAPAddress);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServiceHost", JBWS2150TestSetup.webServiceHost);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServicePort", JBWS2150TestSetup.webServicePort);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServiceSecurePort", JBWS2150TestSetup.webServiceSecurePort);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServicePathRewriteRule", JBWS2150TestSetup.webServicePath);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServiceUriScheme", JBWS2150TestSetup.webServiceUriScheme);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }

   private String getWebServiceHost() {
      return JBWS2150TestSetup.webServiceHost;
   }

   /**
    * Test soap:address rewrite with rewrite engine on
    * 
    * @throws Exception
    */
   public void testRewrite() throws Exception
   {
      setModifySOAPAddress(true);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
            
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/ValidURL", address);
            //avoid invoking on https endpoints as that would require getting the imported wsdl using https...
            if (!wsdlLocation.contains("Secure")) {
               ServiceIface endpoint = getEndpoint(wsdlLocation, "ValidURLService");
               assertEquals(endpoint.echo("hello"), "hello");
            }
            
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidURL", address);
            if (!wsdlLocation.contains("Secure")) {
               ServiceIface endpoint = getEndpoint(wsdlLocation, "InvalidURLService");
               assertEquals(endpoint.echo("hello"), "hello");
            }
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition)); 
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on and the webServiceHost set to a (fake) load balancer host
    * 
    * @throws Exception
    */
   public void testRewriteLoadBalancer() throws Exception
   {
      setModifySOAPAddress(true);
      final String testWebServiceHost = "myloadbalancer.com";
      setWebServiceHost(testWebServiceHost);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", testWebServiceHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + testWebServiceHost + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + testWebServiceHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", testWebServiceHost);
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + testWebServiceHost + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + testWebServiceHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * Test soap:address rewrite with rewrite engine on and the webServiceHost set to jbossws.undefined.host
    * 
    * @throws Exception
    */
   public void testAutoRewrite() throws Exception
   {
      setModifySOAPAddress(true);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost  + ":8080/jaxws-jbws2150/ValidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", serverHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + serverHost + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + serverHost + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "127.0.0.1");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://127.0.0.1:8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://127.0.0.1:8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidURL", address);
            
            //no further checks on the ports forcing https even when getting the wsdl over http
            //as there's no way to tell which port to use for the secure access given the invoked wsdl address (on http)
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            if (!wsdlLocation.contains("Secure")) {
               assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
            }
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * [JBWS-454] Test soap:address URL rewrite according to transport guarantee
    * 
    * @throws Exception
    */
   public void testSecureRewrite() throws Exception
   {
      setModifySOAPAddress(true);
      deploy("jaxws-jbws2150-sec.war");
      try
      {
         final Map<String, String> wsdlLocationsSecMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", getWebServiceHost());
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", getWebServiceHost());
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsSecMap.entrySet()) {
            String wsdlLocationSec = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocationSec);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocationSec), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-sec.war");
      }
   }
   
   /**
    * [JBWS-454] Test soap:address URL rewrite according to transport guarantee
    * 
    * @throws Exception
    */
   public void testSecureRewriteLoadBalancer() throws Exception
   {
      setModifySOAPAddress(true);
      final String testWebServiceHost = "myloadbalancer.com";
      setWebServiceHost(testWebServiceHost);
      deploy("jaxws-jbws2150-sec.war");
      try
      {
         final Map<String, String> wsdlLocationsSecMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", testWebServiceHost);
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", testWebServiceHost);
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", testWebServiceHost);
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", testWebServiceHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", testWebServiceHost);
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", testWebServiceHost);
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", testWebServiceHost);
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", testWebServiceHost);
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsSecMap.entrySet()) {
            String wsdlLocationSec = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocationSec);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150-sec/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocationSec), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-sec.war");
      }
   }

   /**
    * Test soap:address rewrite when the rewrite engine is off
    * 
    * @throws Exception
    */
   public void testNoRewrite() throws Exception
   {
      setModifySOAPAddress(false);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://somehost:80/somepath?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://somehost:443/some-secure-path?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://somehost:80/somepath?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + getWebServiceHost() + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://somehost:443/some-secure-path?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://somehost:80/somepath", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://somehost:443/some-secure-path", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
            
            //check wsdl import address rewrite; here we expect an address using the same base as the one for the port corresponding to the address used to fetch the wsdl
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine is on (code-first endpoint)
    * 
    * @throws Exception
    */
   public void testRewriteCodeFirst() throws Exception
   {
      setModifySOAPAddress(true);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         checkWsdlAndInvokeCodeFirstEndpoint(getServerHost(), getWebServiceHost(), false);
         if (isTestsuiteServerHostLocalhost()) {
            checkWsdlAndInvokeCodeFirstEndpoint("127.0.0.1", getWebServiceHost(), false);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Test soap:address rewrite when the rewrite engine is off (code-first endpoint)
    * 
    * @throws Exception
    */
   public void testNoRewriteCodeFirst() throws Exception
   {
      setModifySOAPAddress(false);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         checkWsdlAndInvokeCodeFirstEndpoint(getServerHost(), getWebServiceHost(), false);
         if (isTestsuiteServerHostLocalhost()) {
            checkWsdlAndInvokeCodeFirstEndpoint("127.0.0.1", getWebServiceHost(), false);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on and the webServiceHost set to a (fake) load balancer host (code-first endpoint)
    * 
    * @throws Exception
    */
   public void testRewriteLoadBalancerCodeFirst() throws Exception
   {
      setModifySOAPAddress(true);
      final String testWebServiceHost = "myloadbalancer.com";
      setWebServiceHost(testWebServiceHost);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         checkWsdlAndInvokeCodeFirstEndpoint(getServerHost(), testWebServiceHost, true);
         if (isTestsuiteServerHostLocalhost()) {
            checkWsdlAndInvokeCodeFirstEndpoint("127.0.0.1", testWebServiceHost, true);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on , the webServiceHost set to a (fake) load balancer host (code-first endpoint) and forced https scheme
    * 
    * @throws Exception
    */
   public void testRewriteLoadBalancerCodeFirstForceHttps() throws Exception
   {
      setModifySOAPAddress(true);
      final String testWebServiceHost = "myloadbalancer.com";
      setWebServiceHost(testWebServiceHost);
      setWebServiceSecurePort(443);
      setWebServiceUriScheme("https");
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         Definition definition = getWSDLDefinition("http://" + getServerHost() + ":8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl");
         String address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
         assertEquals("https://" + testWebServiceHost + "/jaxws-jbws2150-codefirst/CodeFirstService", address);
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Basically the same as the test above; even if rewrite is off, we still get the loadbalancer address in the wsdl
    * for code-first endpoint, as there's no user provided wsdl with a different address in it. 
    * 
    * @throws Exception
    */
   public void testNoRewriteLoadBalancerCodeFirst() throws Exception
   {
      setModifySOAPAddress(false);
      final String testWebServiceHost = "myloadbalancer2.com";
      setWebServiceHost(testWebServiceHost);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         checkWsdlAndInvokeCodeFirstEndpoint(getServerHost(), testWebServiceHost, true);
         if (isTestsuiteServerHostLocalhost()) {
            checkWsdlAndInvokeCodeFirstEndpoint("127.0.0.1", testWebServiceHost, true);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on and the webServiceHost set to jbossws.undefined.host (code-first endpoint)
    * 
    * @throws Exception
    */
   public void testAutoRewriteCodeFirst() throws Exception
   {
      setModifySOAPAddress(true);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         checkWsdlAndInvokeCodeFirstEndpoint(getServerHost(), getServerHost(), true);
         if (isTestsuiteServerHostLocalhost()) {
            checkWsdlAndInvokeCodeFirstEndpoint("127.0.0.1", "127.0.0.1", true);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   /**
    * Test soap:address rewrite when a path rewrite rule is specified.
    *
    * @throws Exception
    */
   public void testRewriteWithPathRule() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150";
      final String sedCmd = "s/jaxws-jbws2150/xx\\/jaxws-jbws2150/g";
      setWebServicePathRewriteRule(sedCmd);
      deploy("jaxws-jbws2150.war");
      try
      {
         final String serverHost = getServerHost();
         final List<String> wsdlLocations = new LinkedList<String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + serverHost + ":8080/" + expectedContext + "/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + serverHost + ":8080/" + expectedContext + "/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/InvalidSecureURL?wsdl=inner.wsdl");

         for (final String wsdlLocation : wsdlLocations)
         {
            Definition definition = getWSDLDefinition(wsdlLocation);

            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + serverHost + ":8080/" + expectedContext + "/ValidURL", address);

            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + serverHost + ":8080/" + expectedContext + "/InvalidURL", address);

            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/ValidSecureURL", address);

            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/InvalidSecureURL", address);

            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * Test soap:address rewrite for code-first endpoints when a path rewrite rule is specified
    *
    * @throws Exception
    */
   public void testRewriteCodeFirstPathRule() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150-codefirst";
      final String sedCmd = "s/jaxws-jbws2150-codefirst/xx\\/jaxws-jbws2150-codefirst/g";
      setWebServicePathRewriteRule(sedCmd);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         final String serverHost = getServerHost();
         final String wsdlLocation = "http://" + serverHost + ":8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl";

         Definition definition = getWSDLDefinition(wsdlLocation);
         String address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
         assertEquals("http://" + serverHost + ":8080/" + expectedContext +"/CodeFirstService", address);
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }

   /**
    * Test soap:address rewrite for code-first endpoints when a path rewrite rule is specified and auto-rewrite is on
    * (wsdl host prop set to ServerConfig.UNDEFINED_HOSTNAME)
    * 
    * @throws Exception
    */
   public void testAutoRewriteCodeFirstPathRule() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150-codefirst";
      final String sedCmd = "s/jaxws-jbws2150-codefirst/xx\\/jaxws-jbws2150-codefirst/g";
      setWebServicePathRewriteRule(sedCmd);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         String serverHost = getServerHost();
         final String wsdlLocation = "http://" + serverHost + ":8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl";

         Definition definition = getWSDLDefinition(wsdlLocation);
         String address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
         assertEquals("http://" + serverHost + ":8080/" + expectedContext +"/CodeFirstService", address);
         if (isTestsuiteServerHostLocalhost()) {
            definition = getWSDLDefinition("http://127.0.0.1:8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl");
            address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
            assertEquals("http://127.0.0.1:8080/" + expectedContext +"/CodeFirstService", address);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }

   /**
    * Test soap:address rewrite with host configured to ServerConfig.UNDEFINED_HOSTNAME and path rewrite rule specified
    * 
    * @throws Exception
    */
   public void testAutoRewritePathRule() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150";
      final String sedCmd = "s/jaxws-jbws2150/xx\\/jaxws-jbws2150/g";
      setWebServicePathRewriteRule(sedCmd);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost  + ":8080/jaxws-jbws2150/ValidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", serverHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + serverHost + ":8080/" + expectedContext + "/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + serverHost + ":8080/" + expectedContext + "/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "127.0.0.1");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://127.0.0.1:8080/" + expectedContext + "/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://127.0.0.1:8080/" + expectedContext + "/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://127.0.0.1:8443/" + expectedContext + "/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://127.0.0.1:8443/" + expectedContext + "/InvalidSecureURL?wsdl=inner.wsdl");
         }
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);

            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + ":8080/" + expectedContext + "/ValidURL", address);

            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/" + expectedContext + "/InvalidURL", address);

            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("http://" + host + ":8080/" + expectedContext + "/ValidSecureURL", address);

            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("http://" + host + ":8080/" + expectedContext + "/InvalidSecureURL", address);

            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            if (!wsdlLocation.contains("Secure")) {
               assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
            }
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on and forced https scheme and secure port
    * 
    * @throws Exception
    */
   public void testRewriteForceHttpsSchemeAndPorts() throws Exception
   {
      setModifySOAPAddress(true);
      setWebServiceUriScheme("https");
      setWebServiceSecurePort(8192);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", getWebServiceHost());
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", getWebServiceHost());
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + getWebServiceHost() + ":8192/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
            //avoid invoking on https endpoints as that would require getting the imported wsdl using https...
            
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("https://" + host + ":8192/jaxws-jbws2150/ValidURL", address);
            
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("https://" + host + ":8192/jaxws-jbws2150/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8192/jaxws-jbws2150/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8192/jaxws-jbws2150/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition)); 
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }
   
   /**
    * Test soap:address rewrite with rewrite engine on, the webServiceHost set to a (fake) load balancer host and forced https uri scheme
    * 
    * @throws Exception
    */
   public void testRewriteLoadBalancerForceHttps() throws Exception
   {
      setModifySOAPAddress(true);
      final String testWebServiceHost = "myloadbalancer.com";
      setWebServiceHost(testWebServiceHost);
      setWebServiceUriScheme("https");
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", testWebServiceHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", testWebServiceHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", testWebServiceHost);
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", testWebServiceHost);
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + testWebServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + host + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * Test soap:address rewrite with rewrite engine on, the webServiceHost set to jbossws.undefined.host and forced http uri scheme
    * 
    * @throws Exception
    */
   public void testAutoRewriteForceHttp() throws Exception
   {
      setModifySOAPAddress(true);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      deploy("jaxws-jbws2150.war");
      try
      {
         final Map<String, String> wsdlLocationsMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsMap.put("http://" + serverHost  + ":8080/jaxws-jbws2150/ValidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", serverHost);
         wsdlLocationsMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", serverHost);
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "127.0.0.1");
            wsdlLocationsMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "127.0.0.1");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "http://127.0.0.1:8080/jaxws-jbws2150/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsMap.entrySet()) {
            String wsdlLocation = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocation);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidURL", address);
            
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/ValidSecureURL", address);
            
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("http://" + host + ":8080/jaxws-jbws2150/InvalidSecureURL", address);
            
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * Test soap:address URL rewrite according to transport guarantee is overridden by forced http uri scheme
    * 
    * @throws Exception
    */
   public void testSecureRewriteOverriddenByForcedHttp() throws Exception
   {
      setModifySOAPAddress(true);
      setWebServiceUriScheme("http");
      setWebServicePort(80);
      deploy("jaxws-jbws2150-sec.war");
      try
      {
         final Map<String, String> wsdlLocationsSecMap = new HashMap<String, String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         final String serverHost = getServerHost();
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", getWebServiceHost());
         wsdlLocationsSecMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", getWebServiceHost());
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         if (isTestsuiteServerHostLocalhost()) {
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", getWebServiceHost());
            wsdlLocationsSecMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", getWebServiceHost());
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/ValidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/InvalidURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/ValidSecureURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/ValidSecureURL?wsdl=inner.wsdl");
            importMap.put("http://127.0.0.1:8080/jaxws-jbws2150-sec/InvalidSecureURL?wsdl", "http://" + getWebServiceHost() + "/jaxws-jbws2150-sec/InvalidSecureURL?wsdl=inner.wsdl");
         }
         
         for (Entry<String, String> entry : wsdlLocationsSecMap.entrySet()) {
            String wsdlLocationSec = entry.getKey();
            String host = entry.getValue();
            Definition definition = getWSDLDefinition(wsdlLocationSec);
   
            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("http://" + host + "/jaxws-jbws2150-sec/ValidURL", address);
   
            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("http://" + host + "/jaxws-jbws2150-sec/InvalidURL", address);
   
            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("http://" + host + "/jaxws-jbws2150-sec/ValidSecureURL", address);
   
            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("http://" + host + "/jaxws-jbws2150-sec/InvalidSecureURL", address);
            
            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocationSec), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-sec.war");
      }
   }
   
   /**
    * Test soap:address rewrite when a path rewrite rule is specified and https scheme is forced.
    *
    * @throws Exception
    */
   public void testRewriteWithPathRuleAndForcedHttps() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150";
      final String sedCmd = "s/jaxws-jbws2150/xx\\/jaxws-jbws2150/g";
      setWebServicePathRewriteRule(sedCmd);
      setWebServiceUriScheme("https");
      deploy("jaxws-jbws2150.war");
      try
      {
         final String serverHost = getServerHost();
         final List<String> wsdlLocations = new LinkedList<String>();
         final Map<String, String> importMap = new HashMap<String, String>();
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl");
         wsdlLocations.add("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/ValidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/InvalidURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/ValidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/ValidSecureURL?wsdl=inner.wsdl");
         importMap.put("http://" + serverHost + ":8080/jaxws-jbws2150/InvalidSecureURL?wsdl", "https://" + serverHost + ":8443/" + expectedContext + "/InvalidSecureURL?wsdl=inner.wsdl");

         for (final String wsdlLocation : wsdlLocations)
         {
            Definition definition = getWSDLDefinition(wsdlLocation);

            String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/ValidURL", address);

            address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/InvalidURL", address);

            address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/ValidSecureURL", address);

            address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
            assertEquals("https://" + serverHost + ":8443/" + expectedContext + "/InvalidSecureURL", address);

            //check wsdl import address rewrite (we expect a rewritten version of the same base address used to fetch the wsdl)
            assertEquals(importMap.get(wsdlLocation), getWsdlImportAddress(definition));
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * Test soap:address rewrite for code-first endpoints when a path rewrite rule is specified, auto-rewrite is on
    * (wsdl host prop set to ServerConfig.UNDEFINED_HOSTNAME) and https scheme is forced
    * 
    * @throws Exception
    */
   public void testAutoRewriteCodeFirstPathRuleAndForcedHttps() throws Exception
   {
      setModifySOAPAddress(true);
      final String expectedContext = "xx/jaxws-jbws2150-codefirst";
      final String sedCmd = "s/jaxws-jbws2150-codefirst/xx\\/jaxws-jbws2150-codefirst/g";
      setWebServicePathRewriteRule(sedCmd);
      setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      setWebServiceUriScheme("https");
      deploy("jaxws-jbws2150-codefirst.war");
      try
      {
         String serverHost = getServerHost();
         final String wsdlLocation = "http://" + serverHost + ":8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl";

         Definition definition = getWSDLDefinition(wsdlLocation);
         String address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
         assertEquals("https://" + serverHost + ":8443/" + expectedContext +"/CodeFirstService", address);
         if (isTestsuiteServerHostLocalhost()) {
            definition = getWSDLDefinition("http://127.0.0.1:8080/jaxws-jbws2150-codefirst/CodeFirstService?wsdl");
            address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
            assertEquals("https://127.0.0.1:8443/" + expectedContext +"/CodeFirstService", address);
         }
      }
      finally
      {
         undeploy("jaxws-jbws2150-codefirst.war");
      }
   }
   
   private void checkWsdlAndInvokeCodeFirstEndpoint(String testHost, String expectedWsdlHost, boolean setTargetAddress) throws Exception {
      final String addr = "http://" + testHost + ":8080/jaxws-jbws2150-codefirst/CodeFirstService";
      final String wsdlLocation = addr + "?wsdl";
      
      Definition definition = getWSDLDefinition(wsdlLocation);
      String address = getPortAddress(definition, "CodeFirstService", "CodeFirstPort");
      assertEquals("http://" + expectedWsdlHost + ":8080/jaxws-jbws2150-codefirst/CodeFirstService", address);
      
      ServiceIface endpoint = getEndpoint(wsdlLocation, "CodeFirstService");
      if (setTargetAddress) {
         ((BindingProvider)endpoint).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addr);
      }
      assertEquals(endpoint.echo("hello"), "hello");
   }
   
   private static boolean isTestsuiteServerHostLocalhost() {
      return "localhost".equals(getServerHost());
   }

   private void setModifySOAPAddress(Boolean value) throws Exception
   {
      Attribute attr = new Attribute("ModifySOAPAddress", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }
   
   private void setWebServiceHost(String value) throws Exception
   {
      Attribute attr = new Attribute("WebServiceHost", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }
   
   private void setWebServicePort(int value) throws Exception
   {
      Attribute attr = new Attribute("WebServicePort", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }
   
   private void setWebServiceSecurePort(int value) throws Exception
   {
      Attribute attr = new Attribute("WebServiceSecurePort", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }
   
   private void setWebServicePathRewriteRule(String value) throws Exception
   {
      Attribute attr = new Attribute("WebServicePathRewriteRule", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }

   private void setWebServiceUriScheme(String value) throws Exception
   {
      Attribute attr = new Attribute("WebServiceUriScheme", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }

   private ServiceIface getEndpoint(String wsdlLocation, String serviceName) throws Exception
   {
      List<WebServiceFeature> features =  new LinkedList<WebServiceFeature>();
      if (isIntegrationCXF()) {
         //Setting UseNewBusFeature as the tests here deploy / undeploy endpoints with different wsdl at the same URL
         //so we need to avoid caching issues related to the WSDLManager in the CXF Bus.
         
         //Service service = Service.create(new URL(wsdlLocation), new QName(NAMESPACE, serviceName), new UseNewBusFeature())
         
         Class<?> clazz = Class.forName("org.jboss.wsf.stack.cxf.client.UseNewBusFeature");
         features.add((WebServiceFeature)clazz.newInstance());
      }
      Service service = Service.create(new URL(wsdlLocation), new QName(NAMESPACE, serviceName), features.toArray(new WebServiceFeature[features.size()]));
      QName portName = service.getPorts().next();
      return service.getPort(portName, ServiceIface.class);
   }
   
   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
      wsdlReader.setFeature("javax.wsdl.importDocuments", false);
      wsdlReader.setFeature("javax.wsdl.verbose", false);

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }

   private String getPortAddress(Definition definition, String serviceName, String portName)
   {
      Port port = definition.getService(new QName(NAMESPACE, serviceName)).getPort(portName);
      return ((SOAPAddressImpl)port.getExtensibilityElements().get(0)).getLocationURI();
   }
   
   @SuppressWarnings("unchecked")
   private String getWsdlImportAddress(Definition definition)
   {
      Collection<Import> imports = definition.getImports(NAMESPACE_IMP);
      if (imports.size() == 1) {
         return imports.iterator().next().getLocationURI();
      }
      return null;
   }

}
