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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
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
 * Test the JAXWS <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class ServiceRefTestCase extends JBossWSTest
{
   private static final String APPCLIENT_DEPLOYMENT = "jaxws-samples-serviceref-appclient";

   private static String fullAppclientDepName;
   
   @ArquillianResource
   private URL baseURL;

   @ArquillianResource
   Deployer deployer;

   @Deployment(name="jaxws-samples-serviceref", order = 1, testable = false)
   public static WebArchive createEndpointDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-serviceref.war");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
            .setWebXML(
               new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/serviceref/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = APPCLIENT_DEPLOYMENT, order = 2, testable = false, managed = false)
   public static EnterpriseArchive createAppclientDeployment() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-serviceref-appclient.jar");
      archive1
         .setManifest(
            new StringAsset("Manifest-Version: 1.0\n"
               + "main-class: org.jboss.test.ws.jaxws.samples.serviceref.ApplicationClient\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.ApplicationClient.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/application-client.xml"), "application-client.xml")
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, APPCLIENT_DEPLOYMENT + ".ear");
      archive.addAsModule(archive1);
      archive.addAsManifestResource(
              new File(JBossWSTestHelper.getTestResourcesDir()
                      + "/jaxws/samples/serviceref/META-INF/permissions-jaxws-samples-serviceref-appclient-jar.xml"), "permissions.xml");
      JBossWSTestHelper.writeToFile(archive);
      fullAppclientDepName = archive.getName() + "#" + archive1.getName();
      return archive;
   }
   
   @Deployment(name = "jaxws-samples-serviceref-ejbclient", order = 3, testable = false)
   public static JavaArchive createEJBClientDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-serviceref-ejbclient.jar");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EJBClient.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EJBRemote.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/ejb-jar.xml"), "ejb-jar.xml")
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/permissions.xml"), "permissions.xml")
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl");
      return archive;
   }
   
   @Deployment(name="jaxws-samples-serviceref-servlet-client", order = 4, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-serviceref-servlet-client.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.EndpointService.class)
         .addClass(org.jboss.test.ws.jaxws.samples.serviceref.ServletClient.class)
         .addAsWebInfResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
         .addAsWebInfResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/servlet-client/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsManifestResource(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/servlet-client/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(
            new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/samples/serviceref/servlet-client/WEB-INF/web.xml"));
      return archive;
   }


   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-serviceref")
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      InputStream inputStream = wsdlURL.openStream();
      assertNotNull(inputStream);
      IOUtils.readAndCloseStream(inputStream);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-serviceref")
   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = getResourceURL("jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl");
      QName qname = new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = service.getPort(Endpoint.class);

      String request = "DynamicProxy";
      String response = port.echo(request);
      assertEquals(request, response);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-serviceref")
   public void testApplicationClient() throws Exception
   {
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      if ("-Djava.security.manager".equals(additionalJVMArgs)) {
         // must pass path to policy file for JBossWSTestHelper to access.
          System.setProperty("securityPolicyfile", JBossWSTestHelper.getTestResourcesDir()
                  + "/jaxws/samples/serviceref/security.policy");
      }

      try
      {
         final OutputStream appclientOS = new ByteArrayOutputStream();
         JBossWSTestHelper.deployAppclient(fullAppclientDepName, appclientOS, "Hello World!");
         // wait till appclient stops
         String appclientLog = appclientOS.toString();
         while (!appclientLog.contains("stopped in")) {
            Thread.sleep(100);
            appclientLog = appclientOS.toString();
         }
         // assert appclient logs
         assertTrue(appclientLog.contains("TEST START"));
         assertTrue(appclientLog.contains("TEST END"));
         assertFalse(appclientLog.contains("not overridden through service-ref"));
         assertFalse(appclientLog.contains("Invalid echo return"));
      }
      finally
      {
         JBossWSTestHelper.undeployAppclient(fullAppclientDepName, false);
      }
   }


   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-serviceref")
   public void testEJBClient() throws Exception
   {      
      InitialContext iniCtx = null;
      try
      {
         iniCtx = getServerInitialContext();
         EJBRemote ejbRemote = (EJBRemote)iniCtx.lookup("jaxws-samples-serviceref-ejbclient//EJBClient!" + EJBRemote.class.getName());

         String helloWorld = "Hello World!";
         Object retObj = ejbRemote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
      }
   }
   
    @Test
    @RunAsClient
    @OperateOnDeployment("jaxws-samples-serviceref-servlet-client")
    public void testServletClient() throws Exception
    {
       URL url = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-samples-serviceref-servlet-client?echo=HelloWorld");
       assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
    }
}
