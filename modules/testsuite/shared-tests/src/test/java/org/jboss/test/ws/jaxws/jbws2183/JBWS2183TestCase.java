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
package org.jboss.test.ws.jaxws.jbws2183;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * @author alessio.soldano@jboss.org
 * @since 13-Oct-2008
 */
public class JBWS2183TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2183TestCase.class, "jaxws-jbws2183.jar");
   }

   public void testWsdl() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2183/TestServiceImpl?wsdl");
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
      for (Iterator<?> it = wsdlDefinition.getAllBindings().values().iterator(); it.hasNext(); )
      {
         List<?> extElements = ((Binding)it.next()).getExtensibilityElements();
         boolean found = false;
         for (int i = 0; i < extElements.size(); i++)
         {
            ExtensibilityElement extElement = (ExtensibilityElement)extElements.get(i);
            if (extElement instanceof SOAP12Binding)
               found = true;
            else if (extElement instanceof SOAPBinding)
               fail("SOAP 1.1 Binding found!");
         }
         assertTrue("SOAP 1.2 Binding not found!",found);
      }
   }
}
