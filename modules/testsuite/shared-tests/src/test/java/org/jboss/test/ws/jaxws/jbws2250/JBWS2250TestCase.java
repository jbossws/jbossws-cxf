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
package org.jboss.test.ws.jaxws.jbws2250;

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
 * [JBWS-2250] Test Case.
 * 
 * The user of JAXBElement causes a NullPointerException on deployment
 * where JBossWS generates a new WSDL.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 7th July 2008
 */
public class JBWS2250TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2250.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2250.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2250.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2250.Id.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2250.Message.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2250.ObjectFactory.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2250/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2250/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2250TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testPortAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2250?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws2250", "EndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      ObjectFactory of = new ObjectFactory();
      Id id = new Id();
      id.setId("003");

      Message message = new Message();
      message.setMessage("Hello");
      message.setMyId(of.createLayoutPerformanceId(id));

      Message retMessage = port.echo(message);
      assertEquals(message.getMessage(), retMessage.getMessage());
      assertEquals(id.getId(), retMessage.getMyId().getValue().getId());
   }

}
