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
package org.jboss.test.ws.jaxws.wrapped.accessor;

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
 * Test different JAXB accesor types.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public class AccessorTestCase extends JBossWSTest
{
   private String targetNS = "http://accessor.wrapped.jaxws.ws.test.jboss.org/";
   private Accessor proxy;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-wrapped-accessor.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.Accessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.AccessorImpl.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessorResponse.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessorResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/wrapped/accessor/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(AccessorTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "AccessorService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-wrapped-accessor/AccessorService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (Accessor) service.getPort(Accessor.class);
   }


   public void testFieldAccessor() throws Exception
   {
      assertEquals("ing123", proxy.fieldAccessor("ing", 123));
   }

   public void testMethodAccessor() throws Exception
   {
      assertEquals("moretesting456", proxy.fieldAccessor("moretesting", 456));
   }
}
