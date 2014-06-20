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
package org.jboss.test.ws.jaxws.jbws3401;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3401] Support for EJBs bundled in .war archives referencing schemas.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class JBWS3401TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3401.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3401.TestEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3401.TestEndpointImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema1.xsd"), "wsdl/schema1.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema2.xsd"), "wsdl/schema2.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema3.xsd"), "wsdl/schema3.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema4.xsd"), "wsdl/schema4.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema5.xsd"), "wsdl/schema5.xsd");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3401TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   private TestEndpoint getPort() throws Exception
   {

      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3401/TestEndpointService/TestEndpoint?wsdl");
      QName serviceName = new QName("http://org.jboss.test.ws/jbws3401", "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);

      return service.getPort(TestEndpoint.class);
   }

   public void testCall() throws Exception
   {
      String message = "Hi";
      String response = getPort().echo(message);
      assertEquals(message, response);
   }

}
