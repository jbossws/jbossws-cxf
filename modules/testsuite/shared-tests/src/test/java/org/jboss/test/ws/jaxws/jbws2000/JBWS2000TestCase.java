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
package org.jboss.test.ws.jaxws.jbws2000;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.samples.xop.doclit.GeneratorDataSource;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 *
 */
public class JBWS2000TestCase extends JBossWSTest
{

   private FileTransferService port;

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws2000.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2000.FileTransferService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2000.FileTransferServiceImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2000/META-INF/permissions.xml"), "permissions.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2000TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   protected void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2000/FileTransfer?wsdl");
         QName serviceName = new QName("http://service.mtom.test.net/", "FileTransferServiceImplService");
         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(FileTransferService.class);

         SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
         binding.setMTOMEnabled(true);
      }
   }

   public void testFileTransfer() throws Exception
   {
      GeneratorDataSource source = new GeneratorDataSource(1024 * 1204 * 8); //avoid going beyond Undertow default max post size
      DataHandler dh = new DataHandler(source);

      boolean success = port.transferFile("JBWS2000.data", dh);
      assertTrue("Failed to transfer file", success);
   }

}
