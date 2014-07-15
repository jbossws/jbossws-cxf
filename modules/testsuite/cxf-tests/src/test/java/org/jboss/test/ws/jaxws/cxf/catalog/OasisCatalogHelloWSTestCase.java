package org.jboss.test.ws.jaxws.cxf.catalog;

import junit.framework.Test;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * User: rsearls
 * Date: 7/9/14
 */
public class OasisCatalogHelloWSTestCase extends JBossWSTest
{
   private final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-catalog/HelloService";

   public static BaseDeployment<?>[] createDeployments() {

      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-catalog.war") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloWsImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.class)
            .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
               "/jaxws/cxf/catalog/META-INF/jax-ws-catalog.xml")),
               "META-INF/jax-ws-catalog.xml")

               // stnd file locations required for successful deployment
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/cxf/catalog/META-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/cxf/catalog/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd")

               // sever side catalog maps to these files.
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/cxf/catalog/META-INF/wsdl/HelloService.wsdl"), "wsdl/foo/HelloService.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
               + "/jaxws/cxf/catalog/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/foo/Hello_schema1.xsd")

         ;
      }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(OasisCatalogHelloWSTestCase.class,
         JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testCatalogOnClientSide() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         URL archiveURL =  JBossWSTestHelper.getArchiveURL("jaxws-cxf-catalog.war");

         // add archive to classpath
         ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
         URLClassLoader urlClassLoader
            = new URLClassLoader(new URL[]{archiveURL}, currentThreadClassLoader);
         Thread.currentThread().setContextClassLoader(urlClassLoader);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);

         OASISCatalogManager catalogManager = bus.getExtension(OASISCatalogManager.class);
         assertNotNull("OASISCatalogManager not provided ", catalogManager);

         String xsd = "http://org.jboss.ws/cxf/catalogclient/ws-addr.xsd";
         String resolvedSchemaLocation = catalogManager.resolveSystem(xsd);
         assertEquals("http://org.foo.bar/client/ws-addr.xsd", resolvedSchemaLocation);

      } finally {
         bus.shutdown(true);
      }
   }

   public void testCatalogInJbosswsCxfClientJar() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);

         // jbossws-cxf-client.Jar is on the classpath by default.
         // cxf processed it during service creation.
         OASISCatalogManager catalogManager = bus.getExtension(OASISCatalogManager.class);
         assertNotNull("OASISCatalogManager not provided ", catalogManager);

         String xsd = "http://ws-i.org/profiles/basic/1.1/ws-addr.xsd";
         String resolvedSchemaLocation = catalogManager.resolveSystem(xsd);
         assertEquals("classpath:/schemas/wsdl/ws-addr.xsd", resolvedSchemaLocation);

      } finally {
         bus.shutdown(true);
      }
   }


   public void testCatalogOnServerSide() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(endpointAddress + "?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         HelloWs proxy = service.getPort(HelloWs.class);
         HelloRequest helloReq = new HelloRequest();
         helloReq.setInput("Anyone home?");
         proxy.doHello(helloReq);

      } finally {
         bus.shutdown(true);
      }
   }
}
