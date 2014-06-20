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
package org.jboss.test.ws.jaxws.samples.provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;

import junit.framework.Test;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 29-Jun-2006
 */
public class ProviderJAXBTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-provider-jaxb.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.provider.ProviderBeanJAXB.class)
               .addClass(org.jboss.test.ws.jaxws.samples.provider.UserType.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/provider/jaxb/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/provider/jaxb/WEB-INF/wsdl/Provider.wsdl"), "wsdl/Provider.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/provider/jaxb/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(ProviderJAXBTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-jaxb/ProviderEndpoint?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   public void testProviderDispatch() throws Exception
   {
      Dispatch<Object> dispatch = createDispatch("ProviderEndpoint");

      UserType user = new UserType();
      user.setString("Kermit");
      user.setQname(new QName("TheFrog"));
      UserType userRes = (UserType)dispatch.invoke(user);
      assertEquals(user.getString(), userRes.getString());
      assertEquals(user.getQname(), userRes.getQname());
   }

   private Dispatch<Object> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/provider";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-jaxb/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress.toExternalForm());
      
      JAXBContext jbc = JAXBContext.newInstance(new Class[] { UserType.class });
      Dispatch<Object> dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD);
      return dispatch;
   }

}
