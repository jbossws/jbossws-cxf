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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * WS-Security sign test case
 *
 * @author alessio.soldano@jboss.com
 * @since 28-May-2008
 */
public final class SignTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-sign";
   
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-sign-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/META-INF/alice.properties"), "alice.properties");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-wsse-sign.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.KeystorePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/bob.jks"), "bob.jks")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/bob.properties"), "bob.properties")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/sign/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(SignTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      assertEquals("Secure Hello World!", proxy.sayHello());
   }
   
   private void setupWsse(ServiceIface proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      
      Map<String,Object> outProps = new HashMap<String,Object>();
      outProps.put("action", "Timestamp Signature");
      outProps.put("user", "alice");
      outProps.put("signaturePropFile", "META-INF/alice.properties");
      outProps.put("signatureKeyIdentifier", "DirectReference");
      outProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.KeystorePasswordCallback");
      outProps.put("signatureParts", "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body");
      WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps); //request
      cxfEndpoint.getOutInterceptors().add(wssOut);
      cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());

      
      Map<String,Object> inProps= new HashMap<String,Object>();
      inProps.put("action", "Timestamp Signature");
      inProps.put("signaturePropFile", "META-INF/alice.properties");
      inProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.KeystorePasswordCallback");
      WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps); //response
      cxfEndpoint.getInInterceptors().add(wssIn);
      cxfEndpoint.getInInterceptors().add(new SAAJInInterceptor());
   }
}
