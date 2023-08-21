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
package org.jboss.test.ws.jaxws.jbws1190;

import java.io.File;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WSDL generated for JSR-181 POJO does not take 'transport-guarantee' in web.xml into account
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1190
 * 
 * @author darran.lofthouse@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 19-October-2006
 */
@RunWith(Arquillian.class)
public class JBWS1190TestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1190.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1190.ConfidentialEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1190.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1190.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1190.JBWS1190Exception.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1190/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointAddress() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName("jboss.ws:context=jaxws-jbws1190,endpoint=Endpoint");
      String address = (String)server.getAttribute(oname, "Address");
      assertTrue("Expected http address, but got: " + address, address.startsWith("http://"));
   }

   @Test
   @RunAsClient
   public void testConfidentialEndpointAddress() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName("jboss.ws:context=jaxws-jbws1190,endpoint=ConfidentialEndpoint");
      String address = (String)server.getAttribute(oname, "Address");
      assertTrue("Expected https address, but got: " + address, address.startsWith("https://"));
   }
}
