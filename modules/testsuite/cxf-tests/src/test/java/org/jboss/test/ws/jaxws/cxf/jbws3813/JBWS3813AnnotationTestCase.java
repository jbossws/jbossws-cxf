/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3813;

import junit.framework.Test;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JBWS3813AnnotationTestCase extends JBossWSTest
{
   public static JBossWSTestHelper.BaseDeployment<?>[] createDeployments() {
      List<JBossWSTestHelper.BaseDeployment<?>> list = new LinkedList<JBossWSTestHelper.BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3813-two.war") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3813.EndpointOne.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3813.EndpointTwoImpl.class)
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
               "/jaxws/cxf/jbws3813/WEB-INF/jaxws-endpoint-config.xml")),
               "jaxws-endpoint-config.xml")
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
               "/jaxws/cxf/jbws3813/WEB-INF/webTwo.xml")),
               "WEB-INF/web.xml");
      }
      });
      return list.toArray(new JBossWSTestHelper.BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3813AnnotationTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testExceptionFlags() throws Exception {
      String endPtAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3813-two/ServiceTwo";
      QName serviceName = new QName("http://org.jboss.ws.jaxws.cxf/jbws3813", "ServiceTwo");
      URL wsdlURL = new URL(endPtAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointOne proxy = service.getPort(EndpointOne.class);
      boolean isFailed = false;
      try
      {
         proxy.echo("foo");
      } catch (javax.xml.ws.soap.SOAPFaultException ex) {
         String text = ex.getFault().getDetail().getFirstChild().getFirstChild().getTextContent();
         isFailed = text.startsWith("java.lang.RuntimeException : my error");
         assertTrue("stack data not found", isFailed);
      } finally {
         assertTrue("test did not fail as required", isFailed);
      }
   }
}

