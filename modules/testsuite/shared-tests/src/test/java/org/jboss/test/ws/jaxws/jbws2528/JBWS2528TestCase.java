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
package org.jboss.test.ws.jaxws.jbws2528;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-2528] Missing parameterOrder in portType/operation
 *
 * http://jira.jboss.org/jira/browse/JBWS-2528
 *
 * @author alessio.soldano@jboss.com
 * @since 12-Mar-2009
 */
public class JBWS2528TestCase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final String EXT = ":".equals( PS ) ? ".sh" : ".bat";

   private String ENDPOINT_CLASS;

   private String JBOSS_HOME;
   private String CLASSES_DIR;
   private String TEST_DIR;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      CLASSES_DIR = System.getProperty("test.classes.directory");
      ENDPOINT_CLASS = "org.jboss.test.ws.jaxws.jbws2528.JBWS2528Endpoint";
      TEST_DIR = createResourceFile("..").getAbsolutePath();
   }

   public void test() throws Exception
   {
      File destDir = new File(TEST_DIR, "wsprovide" + FS + "java");
      String absOutput = destDir.getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsprovide" + EXT + " -k -w -o " + absOutput + " --classpath " + CLASSES_DIR + " " + ENDPOINT_CLASS;
      executeCommand(command, "wsprovide");

      URL wsdlURL = new File(destDir, "JBWS2528EndpointService.wsdl").toURI().toURL();
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      PortType portType = wsdlDefinition.getPortType(new QName("http://jbws2528.jaxws.ws.test.jboss.org/", "JBWS2528Endpoint"));
      Operation op = (Operation)portType.getOperations().get(0);
      @SuppressWarnings("unchecked")
      List<String> parOrder = op.getParameterOrdering();
      assertEquals("id", parOrder.get(0));
      assertEquals("Name", parOrder.get(1));
      assertEquals("Employee", parOrder.get(2));
   }

}
