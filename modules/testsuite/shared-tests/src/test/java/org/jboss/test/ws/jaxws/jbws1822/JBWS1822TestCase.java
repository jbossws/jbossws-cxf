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
package org.jboss.test.ws.jaxws.jbws1822;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1822] Cannot find service endpoint target
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 8, 2008
 */
@RunWith(Arquillian.class)
public final class JBWS1822TestCase extends JBossWSTest
{
   @ArquillianResource
   Deployer deployer;
   static JavaArchive archive1 = null;
   static {
      archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1822-shared.jar");
      archive1
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanIface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanImpl.class);
   }

   @Deployment(name = "jaxws-jbws1822-two-ejb-modules", testable = false, managed = false)
   public static EnterpriseArchive createDeployment2() {
      JavaArchive archive3 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1822-one-ejb3-inside.jar");
      archive3
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3Bean.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface.class);
     // JBossWSTestHelper.writeToFile(archive3);

      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws1822-two-ejb-modules.ear");
      archive
         .addManifest()
         .addAsModule(archive3)        
         .addAsModule(archive1);
      return archive;
   }

   @Deployment(name = "jaxws-jbws1822-one-ejb-module", testable = false, managed = false)
   public static EnterpriseArchive createDeployment1() {
      JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1822-two-ejb3-inside.jar");
      archive2
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanIface.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanImpl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3Bean.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface.class);
      JBossWSTestHelper.writeToFile(archive2);

      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws1822-one-ejb-module.ear");
         archive
            .addManifest()
            .addAsModule(archive2);
      return archive;
   }

   private EJB3RemoteIface getProxy() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS1822", "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws1822?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      return (EJB3RemoteIface)service.getPort(EJB3RemoteIface.class);
   }

   @Test
   @RunAsClient
   public void testOneEjbModule() throws Exception
   {
      deployer.deploy("jaxws-jbws1822-one-ejb-module");
      try
      {
         assertEquals(getProxy().getMessage(), "Injected hello message");
      }
      finally
      {
         deployer.undeploy("jaxws-jbws1822-one-ejb-module");
      }
   }

   @Test
   @RunAsClient
   public void testTwoEjbModules() throws Exception
   {
      deployer.deploy("jaxws-jbws1822-two-ejb-modules");
      try
      {
         assertEquals(getProxy().getMessage(), "Injected hello message");
      }
      finally
      {
         deployer.undeploy("jaxws-jbws1822-two-ejb-modules");
      }
   }

}
