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
package org.jboss.test.ws.jaxws.jbws3223;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3223] Runtime ws client classloader setup (on AS 7)
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Feb-2011
 */
public class EndpointTestCase extends JBossWSTest
{

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3223-servlet.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.common\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws3223.Client.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3223.TestServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3223.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web-ws.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new TestSetup(new JBossWSTestSetup(EndpointTestCase.class, JBossWSTestHelper.writeToFile(createDeployments())));
   }

   public void testWSDLAccess() throws Exception
   {
      readWSDL(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3223?wsdl"));
   }
   
   public void testClientAccess() throws Exception
   {
      String helloWorld = "Hello world!";
      Client client = new Client(false);
      Object retObj = client.run(helloWorld, getResourceURL("jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"));
      assertEquals(helloWorld, retObj);
   }

   public void testServletAccess() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3223-servlet?param=hello-world&clCheck=true");
      assertEquals("hello-world", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   private void readWSDL(URL wsdlURL) throws Exception
   {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
