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
package org.jboss.test.ws.jaxws.jbws1505;

import junit.framework.Test;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Map;

/**
 * [JBWS-1505] Verify wsdl generation on SEI inheritance.
 */
public class JBWS1505TestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.test.ws/jbws1505";
   private Interface2 port;
   private URL wsdlURL;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1505TestCase.class, "jaxws-jbws1505.jar");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1505Service");
      wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1505/JBWS1505Service/JBWS1505EndpointImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(Interface2.class);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      wsdlURL = null;
      port = null;
      super.tearDown();
   }

   /**
    * All methods on the SEI should be mapped.
    *
    * @throws Exception
    */
   public void testWSDLGeneration() throws Exception
   {
      Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlURL.toString());
      Map services = wsdl.getAllServices();
      assertTrue(services.size() == 1); // a simple port
      javax.wsdl.Service service = (javax.wsdl.Service)services.values().iterator().next();
      javax.wsdl.Port port = (javax.wsdl.Port)service.getPorts().values().iterator().next();
      assertTrue(port.getBinding().getBindingOperations().size() == 5); // with five op's
   }

   /**
    * Complex types that inherit from a SEI hirarchy shold expose
    * all members in xml schema.
    *
    * @throws Exception
    */
   public void testTypeInheritance() throws Exception
   {
      CustomType ct = port.getCustomType();
      assertTrue(ct.getMember1() == 1);
      assertTrue(ct.getMember2() == 2);
   }
}
