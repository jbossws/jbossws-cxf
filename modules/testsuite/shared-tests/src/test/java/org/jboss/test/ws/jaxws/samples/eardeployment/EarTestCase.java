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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test ear deployment
 * 
 * [JBWS-1616] Verify correct bahaviour of @WebService.wsdlLocation
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class EarTestCase extends JBossWSTest
{
   public static final class EarTestCaseDeploymentArchive {
      public static final String NAME = getName();
      
      private static String getName() {
         JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-eardeployment-ejb3.jar") { {
            archive
                  .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.jboss.logging\n"))
                  .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.EJB3Bean.class)
                  .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
                  .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/TestService.xsd"), "wsdl/TestService.xsd");
            }
         });
         JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-samples-eardeployment-pojo.war") { {
            archive
                  .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.jboss.logging,org.jboss.ws.common\n"))
                  .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.JSEBean.class)
                  .addClass(org.jboss.test.ws.jaxws.samples.eardeployment.SupportServlet.class)
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/jboss-web.xml"), "jboss-web.xml")
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
                  .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/wsdl/TestService.xsd"), "wsdl/TestService.xsd")
                  .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/WEB-INF/web.xml"));
            }
         });
         return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-eardeployment.ear") { {
            archive
                  .addManifest()
                  .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-samples-eardeployment-ejb3.jar"))
                  .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-samples-eardeployment-pojo.war"))
                  .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/eardeployment/META-INF/permissions.xml"), "permissions.xml");
            }
         });
      }
      
      private EarTestCaseDeploymentArchive() {
         //NO OP
      }
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(EarTestCase.class, EarTestCaseDeploymentArchive.NAME);
   }

   public void testEJB3Endpoint() throws Exception
   {
      String soapAddress = "http://" + getServerHost() + ":8080/earejb3/EndpointService/Endpoint";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      Service service = Service.create(new URL(soapAddress + "?wsdl"), serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      Definition wsdl = getWSDLDefinition(soapAddress + "?wsdl");
      String nsURI = wsdl.getNamespace("jbws1616");
      assertEquals("http://jira.jboss.org/jira/browse/JBWS-1616", nsURI);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testJSEEndpoint() throws Exception
   {
      String soapAddress = "http://" + getServerHost() + ":8080/earjse/JSEBean";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      Service service = Service.create(new URL(soapAddress + "?wsdl"), serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      Definition wsdl = getWSDLDefinition(soapAddress + "?wsdl");
      String nsURI = wsdl.getNamespace("jbws1616");
      assertEquals("http://jira.jboss.org/jira/browse/JBWS-1616", nsURI);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }
}
