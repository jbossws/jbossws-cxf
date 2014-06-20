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
package org.jboss.test.ws.jaxws.jbws1422;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1422] NPE if @WebParam.name like "mX.."
 * 
 * @author Thomas.Diesler@jboss.com 
 */
public class JBWS1422TestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://jbws1422.jaxws.ws.test.jboss.org/";
   private static URL wsdlURL;
   private static IWebsvc port;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws1422.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvc.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1422.IWebsvcImpl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1422TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()), new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
         }
      });
   }

   @Override
   protected void setUp() throws Exception
   {
      if (port == null)
      {
         QName serviceName = new QName(TARGET_NAMESPACE, "JBWS1422Service");
         wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1422/JBWS1422Service/IWebsvcImpl?wsdl");

         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(IWebsvc.class);
      }
   }

   public void testDeployment() throws Exception
   {
      String result = port.cancel("myFooBar");
      assertEquals("Cancelled-myFooBar", result);
   }
}
