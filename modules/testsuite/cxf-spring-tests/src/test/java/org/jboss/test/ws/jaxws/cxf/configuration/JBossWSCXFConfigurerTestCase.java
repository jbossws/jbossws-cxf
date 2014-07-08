/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.configuration;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * To test configure bean by name auto wire
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBossWSCXFConfigurerTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-configuration.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.configuration.ChangeRequestnterceptor.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.configuration.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.configuration.EndpointImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/configuration/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/configuration/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JBossWSCXFConfigurerTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   //JBWS-3033
   public void testConfiguration() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-configuration?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws3033", "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("ChangedRequest", retObj);
   }
}