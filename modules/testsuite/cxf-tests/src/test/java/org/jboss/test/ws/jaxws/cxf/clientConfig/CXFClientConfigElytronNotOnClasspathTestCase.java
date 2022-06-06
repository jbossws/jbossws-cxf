package org.jboss.test.ws.jaxws.cxf.clientConfig;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class testing functionality of WS client when Elytron is not on classpath and provider of {@link org.jboss.wsf.spi.security.ClientConfigProvider} is not found
 *
 * @author dvilkola@redhat.com
 * @since August-2019
 */
@RunWith(Arquillian.class)
public class CXFClientConfigElytronNotOnClasspathTestCase extends JBossWSTest {
   private final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows

   @ArquillianResource
   private URL baseURL;

   public static final String CLIENT_JAR = JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws4179-client.jar") {
      {
         archive
                 .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                         + "Main-Class: org.jboss.test.ws.jaxws.cxf.clientConfig.CXFClientElytronNotOnClasspathMainClass\n"
                         + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.eclipse.angus.activation export services,org.apache.cxf.impl\n"))
                 .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.Hello.class)
                 .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloImpl.class)
                 .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloRequest.class)
                 .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloResponse.class)
                 .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.ObjectFactory.class)
                 .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
                 .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl"), "META-INF/wsdl/hello.wsdl")
                 .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/wildfly-config-http-basic-auth.xml"), "META-INF/wildfly-config.xml")
                 .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/WEB-INF/client-permissions.xml"), "permissions.xml")
                 .addClass(CXFClientElytronNotOnClasspathMainClass.class);
      }
   });

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
              + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.Hello.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloImpl.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloRequest.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.HelloResponse.class)
              .addClass(org.jboss.test.ws.jaxws.cxf.httpauth.ObjectFactory.class)
              .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/WEB-INF/wsdl/hello.wsdl"), "wsdl/hello.wsdl")
              .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/basic/jboss-web.xml"), "jboss-web.xml")
              .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/httpauth/basic/web.xml"));
      return archive;
   }

   protected List<String> runJBossModulesClient() throws Exception {
      StringBuilder sbuf = new StringBuilder();

      // java cmd
      File javaFile = new File(System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      sbuf.append(javaCmd);

      //properties
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      additionalJVMArgs = additionalJVMArgs.replace('\n', ' ');
      sbuf.append(" ").append(additionalJVMArgs);
      sbuf.append(" -Djavax.xml.ws.spi.Provider=").append(ProviderImpl.class.getName());
      sbuf.append(" -Dlog4j.output.dir=").append(System.getProperty("log4j.output.dir"));
      sbuf.append(" -Dwildfly.config.url=").append("META-INF/wildfly-config.xml");

      // ref to jboss-modules jar
      final String jbh = System.getProperty("jboss.home");
      final String jbm = jbh + FS + "modules";
      final String jbmjar = jbh + FS + "jboss-modules.jar";
      final String cp = jbh + FS + "jboss-modules.jar";
      sbuf.append(" -jar ").append(jbmjar);

      // input arguments to jboss-module's main
      sbuf.append(" -mp ").append(jbm);

      // wildfly9 security manage flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      if (additionalJVMArgs.contains("-Djava.security.manager")) {
         sbuf.append(" ").append("-secmgr");
      }

      // our client jar is an input param to jboss-module
      final File f = new File(JBossWSTestHelper.getTestArchiveDir(), CLIENT_JAR);
      sbuf.append(" -jar ").append(f.getAbsolutePath());

      // input args to our client.jar main

      final String command = sbuf.toString();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      executeCommand(command, bout);
      StringTokenizer st = new StringTokenizer(readFirstLine(bout), " ");
      List<String> list = new LinkedList<String>();
      while (st.hasMoreTokens()) {
         list.add(st.nextToken());
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

   @Test
   @RunAsClient
   public void testClientWithoutElytronOnClasspath() throws Exception {
      List<String> list = runJBossModulesClient();
      assertEquals("unauthorized", list.get(0));
      assertEquals("null", list.get(1));
      assertEquals("null", list.get(2));
   }
}
