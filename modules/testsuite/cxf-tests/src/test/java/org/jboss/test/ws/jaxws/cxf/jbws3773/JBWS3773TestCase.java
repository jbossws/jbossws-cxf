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
package org.jboss.test.ws.jaxws.cxf.jbws3773;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.Test;

import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS3773TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3773.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.Greeter.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.GreeterImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3773.TargetServlet.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3773TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testServletRequestAvailability() throws Exception
   {
      Greeter greeter = initPort();

      AddressingProperties addrProperties = new AddressingProperties();

      EndpointReferenceType replyTo = new EndpointReferenceType();
      AttributedURIType replyToURI = new AttributedURIType();
      replyToURI.setValue("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3773/target/replyTo");
      replyTo.setAddress(replyToURI);
      addrProperties.setReplyTo(replyTo);

      BindingProvider provider = (BindingProvider)greeter;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProperties);

      greeter.sayHi("Foo");
      Thread.sleep(1500);
      String result = getTargetServletResult();
      assertTrue("Expected ReplyTo:", result.startsWith("ReplyTo:"));
      assertTrue("Expected <return>http</return>:", result.indexOf("<return>http</return>") > 0);
   }
  
   private Greeter initPort() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3773/SOAPService?wsdl");
      QName qname = new QName("http://jboss.org/hello_world", "SOAPService");
      Service service = Service.create(wsdlURL, qname);
      Greeter greeter = service.getPort(Greeter.class, new AddressingFeature());
      return greeter;
   }

   private String getTargetServletResult() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3773/target/result");
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
