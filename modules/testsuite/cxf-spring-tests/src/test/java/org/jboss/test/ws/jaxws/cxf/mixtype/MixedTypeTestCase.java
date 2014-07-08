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
package org.jboss.test.ws.jaxws.cxf.mixtype;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

public class MixedTypeTestCase extends JBossWSTest
{
   private final String targetNS = "http://org.jboss.ws.jaxws.cxf/mixtype";
   
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-mixtype.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOne.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOneEJB3Impl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOneImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/mixtype/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/mixtype/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/mixtype/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(MixedTypeTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testEndpoint() throws Exception
   {
      URL wsdlOneURL = new URL("http://" + getServerHost() + ":8080/mixtype/jaxws-cxf-mixtype?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)serviceOne.getPort(new QName(targetNS, "EndpointOnePort"), EndpointOne.class);
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount());
   }
   
   public void testEJBEndpoint() throws Exception
   {
      URL wsdlOneURL = new URL("http://" + getServerHost() + ":8080/mixtype/EJBServiceOne/EJBEndpointOne?wsdl");
      QName serviceOneName = new QName(targetNS, "EJBServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)serviceOne.getPort(new QName(targetNS, "EJBEndpointOnePort"), EndpointOne.class);
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount());
   }

 
}