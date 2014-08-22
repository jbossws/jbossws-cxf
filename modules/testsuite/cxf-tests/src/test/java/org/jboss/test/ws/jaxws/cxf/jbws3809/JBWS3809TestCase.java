/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3809;

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

/**
 * User: rsearls
 * Date: 7/25/14
 */
public class JBWS3809TestCase extends JBossWSTest
{
   private final String WEB_CONTEXT = "jaxws-cxf-jbws3809";

   public static JBossWSTestHelper.BaseDeployment<?>[] createDeployments() {

      List<JBossWSTestHelper.BaseDeployment<?>> list = new LinkedList<JBossWSTestHelper.BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3809.jar") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.BasicEjb.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbPortComponentUri.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebContext.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceNoServicename.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.dups.EjbWebServiceNoServicename.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceServicename.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceDupServicename.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3809.EjbWebServiceProvider.class)
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
               "/jaxws/cxf/jbws3809/META-INF/jboss-webservices.xml")),
               "META-INF/jboss-webservices.xml")
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
               "/jaxws/cxf/jbws3809/META-INF/ejb-jar.xml")),
               "META-INF/ejb-jar.xml")
         ;
      }
      });

      return list.toArray(new JBossWSTestHelper.BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3809TestCase.class,
         JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testEjbPortComponentUri() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbPortComponentUriService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/MyEjbPortComponentUri?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbPortComponentUri: confirmed", proxy.getStr("confirmed"));
   }

   public void testEjbWebContext() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebContextService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/MyEjbWebContext?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebContext: confirmed", proxy.getStr("confirmed"));
   }

   public void testEjbWebServiceNoServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebServiceNoServicenameService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/MyEjbWebServiceNoServicenameService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceNoServicename: confirmed", proxy.getStr("confirmed"));
   }

   public void testDupEjbWebServiceNoServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "EjbWebServiceNoServicenameService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/MyDupEjbWebServiceNoServicenameService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("DupEjbWebServiceNoServicename: confirmed", proxy.getStr("confirmed"));
   }

   public void testEjbWebServiceServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "ServicenameEjbWebService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/ServicenameEjbWebService/EjbWebServiceServicename?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceServicename: confirmed", proxy.getStr("confirmed"));
   }

   public void testEjbWebServiceDupServicename() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "ServicenameEjbWebService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/ServicenameEjbWebService/EjbWebServiceDupServicename?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceDupServicename: confirmed", proxy.getStr("confirmed"));
   }


   public void testEjbWebServiceProvider() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws.test", "MyEjbWebServiceProvider");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/"
         + WEB_CONTEXT + "/EjbWebServiceProvider?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      BasicEjb proxy = (BasicEjb)service.getPort(BasicEjb.class);

      assertEquals("EjbWebServiceProvider: confirmed", proxy.getStr("confirmed"));
   }

}
