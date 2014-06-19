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
package org.jboss.test.ws.jaxws.jbws2307;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * JBWS-2307 / JBWS-2997 testcase
 * 
 * @author alessio.soldano@jboss.com
 */
public class JBWS2307MTOMTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-service.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"), "web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2307-client.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"), "web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2307MTOMTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()), true);
   }
   
   public void testMTOM() throws Exception
   {
      assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":8080/jaxws-jbws2307-client/jbws2307?mtom=true").openStream()));
   }
   
   public void testClient() throws Exception
   {
      HttpURLConnection con = (HttpURLConnection)new URL("http://" + getServerHost() + ":8080/jaxws-jbws2307-client/jbws2307").openConnection();
      BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));
      assertEquals("true", isr.readLine());
   }
}
