/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the JAXWS annotation: javax.xml.ws.WebServiceref
 *
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 23-Oct-2005
 */
@RunWith(Arquillian.class)
public class WebServiceRefTestCase extends JBossWSTest
{
   public static final String DEP_WAR = "jaxws-samples-webserviceref";
   public static final String DEP_APPCLIENT_EAR="jaxws-samples-webserviceref-appclient";
   public static final String DEP_EJB3_CLIENT_JAR = "jaxws-samples-webserviceref-ejb3-client";
   public static final String DEP_SERVLET_CLIENT_WAR = "jaxws-samples-webserviceref-servlet-client";
   private static String fullAppclientDepName;

   @ArquillianResource
   private URL baseURL;

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = DEP_WAR, order = 1, testable = false)
   public static WebArchive createEndpointDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webserviceref.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EndpointImpl.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = DEP_APPCLIENT_EAR, order = 2, testable = false, managed = false)
   public static EnterpriseArchive createAppclientDeployment() {
      JavaArchive jarArchive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-webserviceref-appclient.jar");
      jarArchive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "main-class: org.jboss.test.ws.jaxws.samples.webserviceref.EndpointClientOne\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EndpointClientOne.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EndpointService.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/application-client.xml"), "application-client.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/jboss-client.xml"), "jboss-client.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/MultipleEndpoint.wsdl"), "wsdl/MultipleEndpoint.wsdl");
      EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class, DEP_APPCLIENT_EAR + ".ear");
      earArchive.addAsModule(jarArchive);
      earArchive.addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
              + "/jaxws/samples/webserviceref/META-INF/permissions-jaxws-samples-webserviceref-appclient-jar.xml"), "permissions.xml");
      JBossWSTestHelper.writeToFile(earArchive);
      fullAppclientDepName = earArchive.getName() + "#" + jarArchive.getName();
      return earArchive;
   }

   @Deployment(name = DEP_EJB3_CLIENT_JAR, order = 3, testable = false, managed = false)
   public static JavaArchive createEJB3ClientDeployment(){
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-webserviceref-ejb3-client.jar");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EJB3Client.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EJB3Remote.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EndpointService.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/jboss.xml"), "jboss.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/permissions.xml"), "permissions.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/MultipleEndpoint.wsdl"), "wsdl/MultipleEndpoint.wsdl");
      return archive;
   }

   @Deployment(name = DEP_SERVLET_CLIENT_WAR, order = 4, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webserviceref-servlet-client.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.EndpointService.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.MultipleEndpointService.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webserviceref.ServletClient.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/permissions.xml"), "permissions.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/META-INF/wsdl/MultipleEndpoint.wsdl"), "wsdl/MultipleEndpoint.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webserviceref/WEB-INF-client/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP_WAR)
   public void testGeneratedService() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceQName = new QName("http://org.jboss.ws/wsref", "EndpointService");

      EndpointService service = new EndpointService(wsdlURL, serviceQName);
      Endpoint port = service.getEndpointPort();

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP_WAR)
   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceQName = new QName("http://org.jboss.ws/wsref", "EndpointService");
      Service service = Service.create(wsdlURL, serviceQName);
      Endpoint port = service.getPort(Endpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP_WAR)
   public void testApplicationClient() throws Throwable
   {
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      if ("-Djava.security.manager".equals(additionalJVMArgs)) {
         // must pass path to policy file for JBossWSTestHelper to access.
         System.setProperty("securityPolicyfile", JBossWSTestHelper.getTestResourcesDir()
             + "/jaxws/samples/webserviceref/security.policy");
      }

      try
      {
         final String appclientArg = "Hello World!";
         final OutputStream appclientOS = new ByteArrayOutputStream();
         JBossWSTestHelper.deployAppclient(fullAppclientDepName, appclientOS, appclientArg);
         // wait till appclient stops
         String appclientLog = appclientOS.toString();
         while (!appclientLog.contains("stopped in")) {
            Thread.sleep(100);
            appclientLog = appclientOS.toString();
         }
         // assert appclient logs
         assertTrue(!appclientLog.contains("Invalid echo return"));
         assertTrue(appclientLog.contains("TEST START"));
         assertTrue(appclientLog.contains("TEST END"));
      }
      finally
      {
         JBossWSTestHelper.undeployAppclient(fullAppclientDepName, false);
      }
   }
   
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP_WAR)
   public void testEJB3Client() throws Exception
   {
      InitialContext iniCtx = null;
      deployer.deploy(DEP_EJB3_CLIENT_JAR);
      try
      {
         iniCtx = getServerInitialContext();
         EJB3Remote ejb3Remote = (EJB3Remote)iniCtx.lookup("jaxws-samples-webserviceref-ejb3-client//EJB3Client!" + EJB3Remote.class.getName());

         String helloWorld = "Hello World!";
         Object retObj = ejb3Remote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
         deployer.undeploy(DEP_EJB3_CLIENT_JAR);
      }
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP_SERVLET_CLIENT_WAR)
   public void testServletClient() throws Exception
   {
      String text = baseURL.toString().substring(0, baseURL.toString().length()-1);
      URL url = new URL(text + "?echo=HelloWorld");
      assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
   }

}
