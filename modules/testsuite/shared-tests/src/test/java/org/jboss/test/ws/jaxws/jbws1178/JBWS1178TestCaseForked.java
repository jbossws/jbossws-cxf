/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.jbws1178;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1178] Multiple virtual host and soap:address problem
 * [JBWS-864] soap:address in wsdl ignores <url-pattern>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 05-Oct-2006
 */
@RunWith(Arquillian.class)
public class JBWS1178TestCaseForked extends JBossWSTest
{  
   @ArquillianResource
   private Deployer deployer;
   
   private static final String WAR_DEPLOYMENT = "jaxws-jbws1178.war";
   private final ObjectName objectName = ObjectNameFactory.create("jboss.ws:service=ServerConfig");
   private String webServiceHost;

   @Deployment(name = WAR_DEPLOYMENT, testable = false, managed=false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WAR_DEPLOYMENT);
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1178.EndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1178/WEB-INF/web.xml"));
      return archive;
   }

   @Before
   public void setup() throws Exception {
      // Setting the WebServiceHost to an empty string, causes the request host to be used.
      // This must be done before deploy time.
      webServiceHost = (String) getServer().getAttribute(objectName, "WebServiceHost");
      getServer().setAttribute(objectName, new Attribute("WebServiceHost", ""));
      deployer.deploy(WAR_DEPLOYMENT);
   }
   @After
   public void cleanup() throws Exception {
      deployer.undeploy(WAR_DEPLOYMENT);
      getServer().setAttribute(objectName, new Attribute("WebServiceHost", webServiceHost));
      
   }

   @Test
   @RunAsClient
   public void testHostAddress() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      String hostAddress = inetAddr instanceof Inet6Address ? "[" + inetAddr.getHostAddress() + "]" : inetAddr.getHostAddress();
      URL wsdlURL = new URL("http://" + hostAddress + ":" + getServerPort() + "/jaxws-jbws1178/testpattern?wsdl");

      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "EndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext(); 
      URL epURL = new URL((String)reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

      assertEqualsIpv6FormatAware(wsdlURL.getHost(), epURL.getHost());
   }

   @Test
   @RunAsClient
   public void testHostName() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      Assume.assumeFalse("The test works only if there is a hostname available for the machine.",
              inetAddr.getHostAddress().equals(inetAddr.getHostName()));
      URL wsdlURL = new URL("http://" + inetAddr.getHostName() + ":" + getServerPort() + "/jaxws-jbws1178/testpattern?wsdl");

      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "EndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext(); 
      URL epURL = new URL((String)reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

      assertEqualsIpv6FormatAware(wsdlURL.getHost(), epURL.getHost());
   }

   private static void assertEqualsIpv6FormatAware(String expected, String actual) throws UnknownHostException
   {
      String expectedFormatted = JBossWSTestHelper.toIPv6URLFormat(expected);
      String actualFormatted = JBossWSTestHelper.toIPv6URLFormat(actual);

      if (expectedFormatted.startsWith("[") && actualFormatted.startsWith("[")) {
         //compare byte representations of IPv6 addresses to ignore address format differences
         assertEquals(InetAddress.getByName(expectedFormatted).getAddress(), InetAddress.getByName(actualFormatted).getAddress());
      } else {
         assertEquals(expected, actual);
      }
   }
}
