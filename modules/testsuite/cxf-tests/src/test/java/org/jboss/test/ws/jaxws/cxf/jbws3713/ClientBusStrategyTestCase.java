/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClientBusStrategyTestCase extends JBossWSTest
{
   private final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3713.war");
      archive.addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWSImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema2.xsd"), "wsdl/Hello_schema2.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema3.xsd"), "wsdl/Hello_schema3.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema4.xsd"), "wsdl/Hello_schema4.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema5.xsd"), "wsdl/Hello_schema5.xsd");
      return archive;
   }

   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3713-client.jar") { {
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Main-Class: org.jboss.test.ws.jaxws.cxf.jbws3713.TestClient\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl,org.jboss.ws.jaxws-client\n"))
             .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/client-permissions.xml"), "permissions.xml")
             .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.BusCounter.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.Helper.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelperUsignThreadLocal.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.TestClient.class);
      }
   });

   @Test
   @RunAsClient
   public void testClientWithNewBusStrategy() throws Exception
   {
      final int threadPoolSize = 4;
      final int invocations = 5;
      List<Integer> list = runJBossModulesClient(Constants.NEW_BUS_STRATEGY, baseURL + "/HelloService?wsdl", threadPoolSize, invocations);
      assertEquals(threadPoolSize, list.get(0).intValue());
      assertEquals(invocations, list.get(1).intValue());
   }

   @Test
   @RunAsClient
   public void testClientWithTCCLBusStrategy() throws Exception
   {
      final int threadPoolSize = 4;
      final int invocations = 5;
      List<Integer> list = runJBossModulesClient(Constants.TCCL_BUS_STRATEGY, baseURL + "/HelloService?wsdl", threadPoolSize, invocations);
      assertEquals(1, list.get(0).intValue());
      assertEquals(1, list.get(1).intValue());
   }

   @Test
   @RunAsClient
   public void testClientWithThreadBusStrategy() throws Exception
   {
      final int threadPoolSize = 4;
      final int invocations = 5;
      List<Integer> list = runJBossModulesClient(Constants.THREAD_BUS_STRATEGY, baseURL + "/HelloService?wsdl", threadPoolSize, invocations);
      assertEquals(threadPoolSize, list.get(0).intValue());
      assertEquals(threadPoolSize, list.get(1).intValue());
   }
   
   /**
    * Verifies jaxws client bus selection strategy controlled by system properties; in order for checking that,
    * starting a new process is required, as the system property is read once and cached in JBossWS. 
    * 
    * @param strategy
    * @param wsdlAddress
    * @param threadPoolSize
    * @param invocations
    * @return
    * @throws Exception
    */
   protected List<Integer> runJBossModulesClient(final String strategy,
                                                 final String wsdlAddress,
                                                 final int threadPoolSize,
                                                 final int invocations) throws Exception {
      StringBuilder sbuf = new StringBuilder();

      // java cmd
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      sbuf.append(javaCmd);

      //properties
      sbuf.append(" -Djavax.xml.ws.spi.Provider=" + ProviderImpl.class.getName());
      sbuf.append(" -Dlog4j.output.dir=" + System.getProperty("log4j.output.dir"));
      sbuf.append(" -D" + Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY + "=" + strategy);

      // ref to jboss-modules jar
      final String jbh = System.getProperty("jboss.home");
      final String jbm = jbh + FS + "modules";
      final String jbmjar = jbh + FS + "jboss-modules.jar";
      sbuf.append(" -jar " + jbmjar);

      // input arguments to jboss-module's main
      sbuf.append(" -mp " + jbm);

      // wildfly9 security manage flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      additionalJVMArgs =  additionalJVMArgs.replace('\n', ' ');
      String securityManagerDesignator =
          ("-Djava.security.manager".equals(additionalJVMArgs)) ? "-secmgr" : additionalJVMArgs;
      sbuf.append(" " + securityManagerDesignator);

      // our client jar is an input param to jboss-module
      final File f = new File(JBossWSTestHelper.getTestArchiveDir(), CLIENT_JAR);
      sbuf.append(" -jar " + f.getAbsolutePath());

      // input args to our client.jar main
      sbuf.append(" " + wsdlAddress + " " + threadPoolSize + " " + invocations);

      final String command = sbuf.toString();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      executeCommand(command, bout);
      StringTokenizer st = new StringTokenizer(readFirstLine(bout), " ");
      List<Integer> list = new LinkedList<Integer>();
      while (st.hasMoreTokens()) {
         list.add(Integer.parseInt(st.nextToken()));
      }
      return list;
   }

   private static String readFirstLine(ByteArrayOutputStream bout) throws IOException {
      bout.flush();
      final byte[] bytes = bout.toByteArray();
      if (bytes != null) {
          BufferedReader reader = new BufferedReader(new java.io.StringReader(new String(bytes)));
          return reader.readLine();
      } else {
         return null;
      }
   }
}
